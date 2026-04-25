package ai.nora.data

import ai.nora.model.ConversationEntity
import ai.nora.model.MessageEntity
import ai.nora.model.ChatMessage
import ai.nora.model.toDomain
import kotlinx.coroutines.flow.Flow

class DataRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    fun getConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    suspend fun createConversation(title: String, modelPath: String = ""): Long {
        return conversationDao.insert(ConversationEntity(title = title, modelPath = modelPath))
    }

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessages(conversationId)

    suspend fun getMessagesSync(conversationId: Long): List<ChatMessage> =
        messageDao.getMessagesSync(conversationId).map { it.toDomain() }

    suspend fun addMessage(conversationId: Long, role: String, content: String): Long {
        return messageDao.insert(MessageEntity(conversationId = conversationId, role = role, content = content))
    }
}
