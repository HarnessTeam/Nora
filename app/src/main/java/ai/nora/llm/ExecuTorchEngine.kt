package ai.nora.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pytorch.executorch.extension.llm.LlmCallback
import org.pytorch.executorch.extension.llm.LlmModule
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * ExecuTorch LLM Engine — real implementation using LlmModule from executorch-android AAR 1.2.0.
 *
 * LlmModule API (decompiled from AAR):
 *   - LlmModule(modelPath: String, tokenizerPath: String, temperature: Float)
 *   - int generate(prompt: String, seqLen: Int, callback: LlmCallback): int
 *   - void stop()
 *
 * LlmCallback interface:
 *   - void onResult(String token)   — called for each generated token
 *   - void onStats(String json)     — called with performance stats after completion
 *
 * Output post-processing:
 *   - Strips Qwen3 special tokens (<|im_start|>, <|im_end|>, <think|>, </think|>, etc.)
 *   - Skips prompt echo (tokens that match the input prompt)
 *   - Detects repetition loops and triggers early stop
 *   - Trims trailing whitespace
 */
class ExecuTorchEngine(override val context: Context) : LlmEngine {

    override val engineName = "ExecuTorch"

    private val _loadState = MutableStateFlow<EngineLoadState>(EngineLoadState.Unloaded)
    override val loadState: StateFlow<EngineLoadState> = _loadState.asStateFlow()

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var llmModule: LlmModule? = null
    private val isGenerating = AtomicBoolean(false)
    private var currentTemperature = 0.6f

    /**
     * Repetition detection state — shared between generate/generateStream.
     * When repetition is detected, triggers early stop via llmModule.stop().
     */
    private val repetitionState = AtomicReference(RepetitionState())

    /**
     * Accumulates streamed output for repetition detection in generateStream().
     * Only used by stream mode; batch mode uses a local StringBuilder.
     */
    private val streamOutputBuffer = StringBuilder()

    override suspend fun loadModel(
        modelPath: String,
        tokenizerPath: String,
        temperature: Float
    ) {
        withContext(Dispatchers.IO) {
            _loadState.value = EngineLoadState.Loading(0f, "Validating files...")

            try {
                val pteFile = File(modelPath)
                val tokFile = File(tokenizerPath)

                require(pteFile.exists()) { "Model file not found: $modelPath" }
                require(tokFile.exists()) { "Tokenizer file not found: $tokenizerPath" }

                Log.i(TAG, "Model file: ${pteFile.absolutePath}, size: ${pteFile.length() / 1024 / 1024}MB")
                Log.i(TAG, "Tokenizer file: ${tokFile.absolutePath}, size: ${tokFile.length() / 1024}KB")

                _loadState.value = EngineLoadState.Loading(0.2f, "Initializing SoLoader...")
                // SoLoader.init is typically called by the AAR, but ensure it
                try {
                    com.facebook.soloader.SoLoader.init(context, false)
                } catch (_: UnsatisfiedLinkError) {
                    // Already initialized
                } catch (_: IllegalStateException) {
                    // Already initialized
                }

                _loadState.value = EngineLoadState.Loading(0.4f, "Creating LlmModule...")
                currentTemperature = temperature

                // Create LlmModule with the simplest constructor:
                // LlmModule(modelPath, tokenizerPath, temperature)
                llmModule = LlmModule(
                    pteFile.absolutePath,
                    tokFile.absolutePath,
                    currentTemperature
                )

                _loadState.value = EngineLoadState.Loading(0.7f, "Loading model weights (mmap)...")

                // LlmModule loads lazily on first generate(), but we can test it
                val memInfo = getMemoryFootprint()

                _loadState.value = EngineLoadState.Loaded(
                    modelName = pteFile.nameWithoutExtension,
                    memoryBytes = memInfo
                )

                Log.i(TAG, "Model ready: ${pteFile.name}, temperature=$currentTemperature")
            } catch (e: Exception) {
                llmModule = null
                _loadState.value = EngineLoadState.Error(
                    message = e.message ?: "Failed to load model", cause = e
                )
                Log.e(TAG, "Model load failed", e)
            }
        }
    }

    override suspend fun generate(prompt: String, maxTokens: Int): String {
        check(llmModule != null) { "Model not loaded." }
        isGenerating.set(true)
        repetitionState.set(RepetitionState())
        return withContext(Dispatchers.IO) {
            try {
                val sb = StringBuilder()
                val state = OutputPostProcessor(
                    promptLength = prompt.length,
                    onStopGeneration = { llmModule?.stop() }
                )
                val result = llmModule!!.generate(
                    prompt,
                    maxTokens,
                    object : LlmCallback {
                        override fun onResult(token: String) {
                            val processed = state.processToken(token)
                            if (processed != null) {
                                sb.append(processed)
                                // Check repetition
                                if (detectAndStopOnRepetition(sb)) return
                            }
                        }

                        override fun onStats(stats: String) {
                            Log.i(TAG, "Stats: $stats")
                        }
                    }
                )
                Log.i(TAG, "Generate returned: $result, tokens generated: ${state.tokenCount}")
                cleanOutput(sb.toString())
            } finally {
                isGenerating.set(false)
            }
        }
    }

    override fun generateStream(
        prompt: String,
        maxTokens: Int,
        onToken: (String) -> Unit
    ): Job {
        check(llmModule != null) { "Model not loaded." }
        repetitionState.set(RepetitionState())
        streamOutputBuffer.clear()
        return engineScope.launch(Dispatchers.IO) {
            isGenerating.set(true)
            try {
                val state = OutputPostProcessor(
                    promptLength = prompt.length,
                    onStopGeneration = { llmModule?.stop() }
                )
                val result = llmModule!!.generate(
                    prompt,
                    maxTokens,
                    object : LlmCallback {
                        override fun onResult(token: String) {
                            val processed = state.processToken(token)
                            if (processed != null) {
                                streamOutputBuffer.append(processed)
                                onToken(processed)
                                // Check repetition in stream mode too
                                detectAndStopOnRepetition(streamOutputBuffer)
                            }
                        }

                        override fun onStats(stats: String) {
                            Log.i(TAG, "Stats: $stats")
                        }
                    }
                )
                Log.i(TAG, "Stream generate returned: $result, tokens generated: ${state.tokenCount}")
            } catch (e: Exception) {
                Log.e(TAG, "Stream generate failed", e)
                onToken("\n[Error: ${e.message}]")
            } finally {
                isGenerating.set(false)
            }
        }
    }

    override fun stop() {
        if (isGenerating.get()) {
            try {
                llmModule?.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Stop failed", e)
            }
            isGenerating.set(false)
            Log.i(TAG, "Generation stopped")
        }
    }

    override fun unload() {
        stop()
        llmModule = null
        _loadState.value = EngineLoadState.Unloaded
        Log.i(TAG, "Model unloaded")
    }

    // ────────────────────────────────────────────────────────────────
    // Output post-processing: strip prompt echo + special tokens
    // ────────────────────────────────────────────────────────────────

    /**
     * Stateful processor that handles prompt echo skipping and special token stripping.
     *
     * ExecuTorch's LlmModule.generate() echoes the input prompt tokens via onResult().
     * We need to skip everything up to and including the "<|im_start|>assistant\n" marker
     * (which is the last line of our prompt), then only forward real generated tokens.
     *
     * Key insight: the tokenizer may encode/decode special tokens differently than their
     * raw string form. So we use MULTIPLE fallback strategies:
     *   1. Exact match of "<|im_start|>assistant\n"
     *   2. Fuzzy match: look for "assistant" preceded by a newline (tokenizer artifacts)
     *   3. If nothing matches after the entire prompt length, force generate mode
     *      but AGGRESSIVELY strip all prompt content using regex
     *
     * Additionally, Qwen3 may output `<think|>...` reasoning blocks and `<|im_end|>`
     * markers that should be stripped from the display.
     */
    private class OutputPostProcessor(
        /** Length of the original prompt — used to estimate when echo ends */
        private val promptLength: Int = 0,
        /** Callback to request generation stop (e.g., on <|im_end|> or role escape) */
        private val onStopGeneration: () -> Unit = {}
    ) {
        /** Buffer for accumulating tokens to detect the assistant marker boundary */
        private val buffer = StringBuilder()
        /** Whether we've found the assistant marker and started forwarding real tokens */
        private var generating = false
        /** Whether we're inside a <think|> block that should be suppressed */
        private var inThinkBlock = false
        /** Think block buffer (discarded, but need to track state) */
        private val thinkBuffer = StringBuilder()
        /** Count of real tokens forwarded (for logging) */
        var tokenCount = 0
            private set

        companion object {
            private const val ASSISTANT_MARKER = "<|im_start|>assistant\n"
            private const val ASSISTANT_MARKER_ALT = "<|im_start|>assistant"
            private const val THINK_OPEN = "<think"
            private const val THINK_CLOSE = "</think"
            private const val IM_END = "<|im_end|>"
            // Patterns that indicate the model has left its role and is hallucinating dialogue
            private val ROLE_ESCAPE_PATTERNS = listOf("Human:", "human:", "Assistant:", "assistant:")

            /**
             * Aggressive regex to strip ALL Qwen3 chat template artifacts.
             * This is used as a safety net when the assistant marker fails to match.
             *
             * Patterns matched:
             *   - <|im_start|>role\n  (any role tag + newline)
             *   - <|im_start|>role\n...content...<|im_end|>  (full message blocks)
             *   - <|im_end|>  (end markers)
             *   - <think...>...</think...>  (reasoning blocks)
             *   - <|...|>  (any remaining special token)
             */
            private val STRIP_ALL_SPECIAL = Regex(
                """<\|im_start\|>\S*\n.*?(?=<\|im_start\|>|<\|im_end\|>|$)""" +
                """|<\|im_end\|>""" +
                """|<think[^>]*>.*?</think[^>]*>""" +
                """|<\|[^>]+\|>""",
                setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
            )
        }

        fun processToken(token: String): String? {
            buffer.append(token)

            if (!generating) {
                // Still in prompt echo phase — look for assistant marker
                val bufStr = buffer.toString()

                // Strategy 1: Exact match — use LAST occurrence because prompt
                // contains historical <assistant> markers from prior conversation turns.
                // Only the LAST one marks where the model should begin generating.
                val markerIdx = bufStr.lastIndexOf(ASSISTANT_MARKER)
                if (markerIdx >= 0) {
                    generating = true
                    val afterMarker = bufStr.substring(markerIdx + ASSISTANT_MARKER.length)
                    buffer.clear()
                    buffer.append(afterMarker)
                    Log.i(TAG, "OutputPostProcessor: found exact assistant marker at $markerIdx (last of ${bufStr.count { true }} chars)")
                    if (afterMarker.isNotEmpty()) {
                        return filterSpecialTokens(afterMarker)
                    }
                    return null
                }

                // Strategy 2: Fuzzy match — use LAST occurrence (same reason as Strategy 1)
                val fuzzyIdx = bufStr.lastIndexOf(ASSISTANT_MARKER_ALT)
                if (fuzzyIdx >= 0) {
                    // Find the newline after "assistant"
                    val afterAssistant = fuzzyIdx + ASSISTANT_MARKER_ALT.length
                    val newlineIdx = bufStr.indexOf('\n', afterAssistant)
                    val afterMarker = if (newlineIdx >= 0) {
                        bufStr.substring(newlineIdx + 1)
                    } else {
                        // No newline yet — might come in next token
                        return null
                    }
                    generating = true
                    buffer.clear()
                    buffer.append(afterMarker)
                    Log.i(TAG, "OutputPostProcessor: found fuzzy assistant marker at $fuzzyIdx")
                    if (afterMarker.isNotEmpty()) {
                        return filterSpecialTokens(afterMarker)
                    }
                    return null
                }

                // Strategy 3: If buffer exceeds prompt length + margin, force generate mode
                // Use the prompt length to estimate when echo should be done
                val forceThreshold = maxOf(100, promptLength + 50)
                if (buffer.length > forceThreshold) {
                    Log.w(TAG, "OutputPostProcessor: forcing generate mode after ${buffer.length} chars (prompt was $promptLength)")
                    generating = true
                    // Aggressively strip ALL special tokens from the buffer
                    val cleaned = stripAllPromptContent(buffer.toString())
                    buffer.clear()
                    buffer.append(cleaned)
                    return if (cleaned.isNotEmpty()) {
                        // Re-run through special token filter for think blocks etc
                        filterSpecialTokens(cleaned)
                    } else null
                }
                return null
            }

            // We're in generation phase — filter special tokens
            return filterSpecialTokens(token)
        }

        /**
         * Aggressively strip all Qwen3 chat template content.
         * Used when the assistant marker fails to match.
         * This removes entire message blocks (role + content + markers).
         */
        private fun stripAllPromptContent(text: String): String {
            return STRIP_ALL_SPECIAL.replace(text, "").trim()
        }

        /**
         * Filter Qwen3 special tokens from generated output.
         * Handles <think|>...</think|> blocks (suppresses them) and <|im_end|> (truncates + stops).
         */
        private fun filterSpecialTokens(token: String): String? {
            if (inThinkBlock) {
                thinkBuffer.append(token)
                if (thinkBuffer.contains(THINK_CLOSE)) {
                    inThinkBlock = false
                    thinkBuffer.clear()
                }
                return null // Suppress everything inside think blocks
            }

            // Check for <|im_end|> — model is done, truncate output AND stop generation
            if (token.contains(IM_END)) {
                Log.i(TAG, "filterSpecialTokens: detected <|im_end|>, stopping generation")
                onStopGeneration()
                val idx = token.indexOf(IM_END)
                val before = token.substring(0, idx).trim()
                // Also check if there's a think block starting before im_end
                val thinkIdx = before.indexOf(THINK_OPEN)
                if (thinkIdx >= 0) {
                    val cleaned = before.substring(0, thinkIdx).trim()
                    return if (cleaned.isNotEmpty()) { tokenCount++; cleaned } else null
                }
                return if (before.isNotEmpty()) { tokenCount++; before } else null
            }

            // Check for <think|> block start
            val thinkIdx = token.indexOf(THINK_OPEN)
            if (thinkIdx >= 0) {
                val before = token.substring(0, thinkIdx).trim()
                val after = token.substring(thinkIdx)

                // Check if think block also closes in this same token
                if (after.contains(THINK_CLOSE)) {
                    // Think block opens and closes in same token — extract any content after
                    val closeIdx = after.indexOf(THINK_CLOSE)
                    val afterThink = after.substring(closeIdx + THINK_CLOSE.length).trim()
                    if (before.isNotEmpty()) { tokenCount++; return before }
                    return if (afterThink.isNotEmpty()) { tokenCount++; afterThink } else null
                }

                // Think block starts here — forward anything before it, then suppress
                inThinkBlock = true
                thinkBuffer.clear()
                thinkBuffer.append(after)
                return if (before.isNotEmpty()) { tokenCount++; before } else null
            }

            // Safety net: detect role-escape patterns (model hallucinating new dialogue)
            // This catches cases where <|im_end|> was missed and the model starts generating
            // "Human: ..." or "Assistant: ..." as if starting a new conversation turn
            for (pattern in ROLE_ESCAPE_PATTERNS) {
                if (token.trimStart().startsWith(pattern)) {
                    Log.w(TAG, "filterSpecialTokens: role escape detected [${token.trimStart().take(20)}], stopping generation")
                    onStopGeneration()
                    return null
                }
            }

            tokenCount++
            return token
        }
        } // OutputPostProcessor class

    // ────────────────────────────────────────────────────────────────
    // Repetition detection & early stop
    // ────────────────────────────────────────────────────────────────

    /**
     * Detects repetitive output loops and triggers early stop.
     *
     * Strategy: compare the last N characters against the previous N characters.
     * If similarity ratio exceeds threshold, call llmModule.stop().
     *
     * Parameters tuned for Qwen3-0.6B:
     * - WINDOW_SIZE = 64: compare 64-char windows (roughly 2-3 sentences)
     * - MAX_CONSECUTIVE_HITS = 4: stop after 4 consecutive matches
     */
    private data class RepetitionState(
        val consecutiveHits: Int = 0,
        val lastWindow: String = ""
    )

    private fun detectAndStopOnRepetition(sb: StringBuilder): Boolean {
        val text = sb.toString()
        val windowSize = 64
        val maxHits = 4

        if (text.length < windowSize * 2) return false

        val currentWindow = text.takeLast(windowSize)
        val prevState = repetitionState.get()

        if (currentWindow == prevState.lastWindow && prevState.lastWindow.isNotBlank()) {
            val newState = prevState.copy(consecutiveHits = prevState.consecutiveHits + 1)
            repetitionState.set(newState)

            if (newState.consecutiveHits >= maxHits) {
                Log.w(TAG, "Repetition detected: same $windowSize-char window repeated ${newState.consecutiveHits} times — stopping")
                llmModule?.stop()
                // Remove the duplicated content from output
                val truncateAt = text.length - windowSize * newState.consecutiveHits
                if (truncateAt > 0) {
                    sb.delete(truncateAt, text.length)
                }
                return true
            }
        } else {
            // Reset on non-match
            repetitionState.set(RepetitionState(lastWindow = currentWindow))
        }
        return false
    }

    // ────────────────────────────────────────────────────────────────
    // Final output cleanup
    // ────────────────────────────────────────────────────────────────

    /**
     * Clean up the final output string:
     * - Trim leading/trailing whitespace
     * - Remove any remaining special token fragments using aggressive regex
     */
    private fun cleanOutput(text: String): String {
        var result = text.trim()
        // Use the same aggressive regex as OutputPostProcessor for consistency
        result = result
            .replace(Regex("""<\|im_start\|>\S*\n.*?(?=<\|im_start\|>|<\|im_end\|>|$)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)), "")
            .replace(Regex("<\\|im_end\\|>"), "")
            .replace(Regex("<think[^>]*>.*?</think[^>]*>", setOf(RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("<\\|[^>]+\\|>"), "")
        return result.trim()
    }

    // ────────────────────────────────────────────────────────────────
    // Utilities
    // ────────────────────────────────────────────────────────────────

    private fun getMemoryFootprint(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

    companion object {
        private const val TAG = "ExecuTorchEngine"
    }
}
