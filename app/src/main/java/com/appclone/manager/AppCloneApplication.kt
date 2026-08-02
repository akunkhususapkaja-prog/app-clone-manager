package com.appclone.manager

import android.app.Application
import android.util.Log
import com.appclone.manager.engine.VirtualEngine

/**
 * AppCloneApplication - Custom Application class for App Clone Manager.
 * 
 * Initializes the virtual engine and sets up global configurations.
 */
class AppCloneApplication : Application() {

    companion object {
        private const val TAG = "AppCloneApplication"
        private lateinit var instance: AppCloneApplication

        fun getInstance(): AppCloneApplication = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize the virtual engine
        val engineStarted = VirtualEngine.initialize(this)
        if (engineStarted) {
            Log.d(TAG, "App Clone Manager started successfully")
        } else {
            Log.e(TAG, "Failed to initialize Virtual Engine")
        }
    }
}
