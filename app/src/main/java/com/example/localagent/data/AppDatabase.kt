package com.example.localagent.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.localagent.model.ConversationEntity
import com.example.localagent.model.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
