package ai.nora

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.facebook.soloader.SoLoader
import ai.nora.data.AppDatabase
import ai.nora.data.DataRepository

class NoraApp : Application() {

    companion object {
        lateinit var instance: NoraApp
            private set
    }

    lateinit var database: AppDatabase
        private set

    lateinit var dataRepository: DataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nora_database"
        ).build()

        // Initialize DataRepository
        dataRepository = DataRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao()
        )

        Log.i("Nora", "Room database initialized: nora_database")

        try {
            SoLoader.init(this, false)
            Log.i("Nora", "SoLoader initialized successfully")
        } catch (e: Exception) {
            Log.e("Nora", "SoLoader initialization failed", e)
        }
    }
}
