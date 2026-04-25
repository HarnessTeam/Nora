package ai.nora.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Chat message data model used across UI and data layers.
 */
data class ChatMessage(
    val role: String,      // "user" | "assistant" | "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

/**
 * Room entity for persisted messages.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for conversations.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelPath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Convert entity to domain model
 */
fun MessageEntity.toDomain(): ChatMessage = ChatMessage(
    role = role,
    content = content,
    timestamp = createdAt
)
