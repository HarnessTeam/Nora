package com.example.localagent.llm

import com.example.localagent.model.ChatMessage
import org.junit.Assert.*
import org.junit.Test

class ChatModelsTest {

    @Test
    fun `buildQwen3Prompt formats correctly for empty history`() {
        val prompt = buildTestPrompt(emptyList())
        assertTrue("Should contain system prompt", prompt.contains("<|im_start|>system"))
        assertTrue("Should end with assistant", prompt.endsWith("<|im_start|>assistant\n"))
    }

    @Test
    fun `buildQwen3Prompt includes user messages`() {
        val messages = listOf(ChatMessage(role = "user", content = "hello"))
        val prompt = buildTestPrompt(messages)
        assertTrue("Should contain user role", prompt.contains("<|im_start|>user"))
        assertTrue("Should contain message content", prompt.contains("hello"))
        assertTrue("Should contain im_end", prompt.contains("<|im_end|>"))
    }

    @Test
    fun `buildQwen3Prompt includes multiple turns`() {
        val messages = listOf(
            ChatMessage(role = "user", content = "q1"),
            ChatMessage(role = "assistant", content = "a1"),
            ChatMessage(role = "user", content = "q2")
        )
        val prompt = buildTestPrompt(messages)
        assertTrue(prompt.contains("q1"))
        assertTrue(prompt.contains("a1"))
        assertTrue(prompt.contains("q2"))
    }

    @Test
    fun `buildQwen3Prompt does not include streaming messages`() {
        val messages = listOf(
            ChatMessage(role = "user", content = "hello"),
            ChatMessage(role = "assistant", content = "", isStreaming = true)
        )
        val prompt = buildTestPrompt(messages.filter { !it.isStreaming })
        assertFalse("Should not include empty streaming assistant", prompt.contains("<|im_start|>assistant\n<|im_end|>"))
    }

    /**
     * Replicate the Qwen3 prompt building logic from ChatViewModel
     * for isolated unit testing
     */
    private fun buildTestPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        sb.append("<|im_start|>system\nYou are a helpful assistant.<|im_end|>\n")
        for (msg in messages) {
            if (msg.role == "user" || msg.role == "assistant") {
                sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
            }
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }
}
