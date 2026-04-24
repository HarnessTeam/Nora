package com.example.localagent

import android.app.Application
import android.util.Log
import com.facebook.soloader.SoLoader

class LocalAgentApp : Application() {

    companion object {
        lateinit var instance: LocalAgentApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            SoLoader.init(this, false)
            Log.i("LocalAgent", "SoLoader initialized successfully")
        } catch (e: Exception) {
            Log.e("LocalAgent", "SoLoader initialization failed", e)
        }
    }
}
