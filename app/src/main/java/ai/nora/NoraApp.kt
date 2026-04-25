package ai.nora

import android.app.Application
import android.util.Log
import com.facebook.soloader.SoLoader

class NoraApp : Application() {

    companion object {
        lateinit var instance: NoraApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            SoLoader.init(this, false)
            Log.i("Nora", "SoLoader initialized successfully")
        } catch (e: Exception) {
            Log.e("Nora", "SoLoader initialization failed", e)
        }
    }
}
