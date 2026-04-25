package ai.nora.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.nora.llm.ExecuTorchEngine
import ai.nora.llm.LlmEngine
import ai.nora.model.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false
)

class ChatViewModel(
    private val llmEngine: LlmEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    companion object {
        /** Max tokens for streaming — 512 is reasonable for 0.6B model on mobile */
        private const val MAX_TOKENS_STREAM = 512
        /** Max tokens for non-streaming (batch) */
        private const val MAX_TOKENS_BATCH = 512
        /** Max conversation turns to include in prompt context */
        private const val MAX_CONTEXT_TURNS = 10
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        val userMsg = ChatMessage(role = "user", content = trimmed)
        val assistantMsg = ChatMessage(role = "assistant", content = "", isStreaming = true)
        val updatedMessages = _uiState.value.messages + userMsg + assistantMsg
        _uiState.value = _uiState.value.copy(messages = updatedMessages, inputText = "", isGenerating = true)

        viewModelScope.launch {
            try {
                val prompt = buildQwen3Prompt(updatedMessages.filter { !it.isStreaming })
                val reply = llmEngine.generate(prompt, maxTokens = MAX_TOKENS_BATCH)
                val msgs = _uiState.value.messages.toMutableList()
                val lastIdx = msgs.indexOfLast { it.isStreaming }
                if (lastIdx >= 0) msgs[lastIdx] = msgs[lastIdx].copy(content = reply, isStreaming = false)
                _uiState.value = _uiState.value.copy(messages = msgs, isGenerating = false)
            } catch (e: Exception) {
                val msgs = _uiState.value.messages.toMutableList()
                val lastIdx = msgs.indexOfLast { it.isStreaming }
                if (lastIdx >= 0) msgs[lastIdx] = msgs[lastIdx].copy(content = "Error: ${e.message}", isStreaming = false)
                _uiState.value = _uiState.value.copy(messages = msgs, isGenerating = false)
            }
        }
    }

    fun sendMessageStream(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        val userMsg = ChatMessage(role = "user", content = trimmed)
        val assistantMsg = ChatMessage(role = "assistant", content = "", isStreaming = true)
        val updatedMessages = _uiState.value.messages + userMsg + assistantMsg
        _uiState.value = _uiState.value.copy(messages = updatedMessages, inputText = "", isGenerating = true)

        viewModelScope.launch {
            val prompt = buildQwen3Prompt(updatedMessages.filter { !it.isStreaming })
            streamJob = llmEngine.generateStream(prompt, maxTokens = MAX_TOKENS_STREAM) { token ->
                val msgs = _uiState.value.messages.toMutableList()
                val lastIdx = msgs.indexOfLast { it.isStreaming }
                if (lastIdx >= 0) msgs[lastIdx] = msgs[lastIdx].copy(content = msgs[lastIdx].content + token)
                _uiState.value = _uiState.value.copy(messages = msgs)
            }
            streamJob?.join()
            // Final cleanup: strip any residual special tokens that leaked through streaming
            val finalMsgs = _uiState.value.messages.toMutableList()
            val lastIdx = finalMsgs.indexOfLast { it.isStreaming }
            if (lastIdx >= 0) {
                val rawContent = finalMsgs[lastIdx].content
                val cleanedContent = cleanStreamOutput(rawContent)
                finalMsgs[lastIdx] = finalMsgs[lastIdx].copy(
                    content = cleanedContent,
                    isStreaming = false
                )
            }
            _uiState.value = _uiState.value.copy(messages = finalMsgs, isGenerating = false)
        }
    }

    fun stopGeneration() {
        llmEngine.stop()
        streamJob?.cancel()
        streamJob = null
        val msgs = _uiState.value.messages.toMutableList()
        val lastIdx = msgs.indexOfLast { it.isStreaming }
        if (lastIdx >= 0) msgs[lastIdx] = msgs[lastIdx].copy(isStreaming = false)
        _uiState.value = _uiState.value.copy(messages = msgs, isGenerating = false)
    }

    fun updateInput(text: String) { _uiState.value = _uiState.value.copy(inputText = text) }
    fun clearChat() { _uiState.value = ChatUiState() }

    /**
     * Final cleanup for streamed output — strips any residual special tokens
     * that may have leaked through during streaming.
     */
    private fun cleanStreamOutput(text: String): String {
        return text.trim()
            .replace(Regex("""<\|im_start\|>\S*\n.*?(?=<\|im_start\|>|<\|im_end\|>|$)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)), "")
            .replace(Regex("<\\|im_end\\|>"), "")
            .replace(Regex("<think[^>]*>.*?</think[^>]*>", setOf(RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("<\\|[^>]+\\|>"), "")
            .trim()
    }

    /**
     * Build Qwen3 ChatML prompt with:
     * - `/no_think` system suffix to disable chain-of-thought (prevents <think|> blocks
     *   that waste tokens on a 0.6B model and confuse the output)
     * - Context window limiting to prevent prompt from exceeding model's effective context
     * - Proper assistant marker at the end for generation
     */
    private fun buildQwen3Prompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()

        // System prompt with /no_think to disable thinking mode on Qwen3
        // Qwen3-0.6B's thinking capability is weak and wastes generation budget
        sb.append("<|im_start|>system\nYou are a helpful assistant. /no_think<|im_end|>\n")

        // Include only the most recent turns to stay within context limits
        val recentMessages = if (messages.size > MAX_CONTEXT_TURNS) {
            messages.takeLast(MAX_CONTEXT_TURNS)
        } else {
            messages
        }

        for (msg in recentMessages) {
            if (msg.role == "user" || msg.role == "assistant") {
                sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
            }
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }
}
