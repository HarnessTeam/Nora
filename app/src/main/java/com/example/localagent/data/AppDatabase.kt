package ai.nora.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ai.nora.model.ConversationEntity
import ai.nora.model.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
