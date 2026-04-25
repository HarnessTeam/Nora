package ai.nora.llm

import kotlinx.coroutines.flow.StateFlow

/**
 * Core LLM Engine interface — abstraction layer for any inference backend.
 * Phase 1: ExecuTorch implementation
 * Future: MNN-LLM, llama.cpp fallback
 */
interface LlmEngine {

    val engineName: String

    val loadState: StateFlow<EngineLoadState>

    /**
     * Load model from file paths.
     * Must be called on IO dispatcher.
     */
    suspend fun loadModel(
        modelPath: String,
        tokenizerPath: String,
        temperature: Float = 0.6f
    )

    /**
     * Non-streaming generation (Phase 1).
     * Returns full response when complete.
     */
    suspend fun generate(
        prompt: String,
        maxTokens: Int = 512
    ): String

    /**
     * Streaming generation (Phase 2).
     * Returns a Job that can be cancelled.
     */
    fun generateStream(
        prompt: String,
        maxTokens: Int = 512,
        onToken: (String) -> Unit
    ): kotlinx.coroutines.Job

    /** Interrupt current generation */
    fun stop()

    /** Unload model, release memory */
    fun unload()
}

sealed class EngineLoadState {
    data object Unloaded : EngineLoadState()
    data class Loading(val progress: Float = 0f, val message: String = "") : EngineLoadState()
    data class Loaded(val modelName: String, val memoryBytes: Long) : EngineLoadState()
    data class Error(val message: String, val cause: Throwable? = null) : EngineLoadState()
}
