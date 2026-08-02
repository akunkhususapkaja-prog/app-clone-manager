package com.appclone.manager.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.appclone.manager.R

/**
 * VirtualEngineService - Foreground service that manages virtual app instances.
 * 
 * This service runs in a separate process (":virtual") to provide:
 * - Process isolation for virtual instances
 * - Lifecycle management of cloned apps
 * - Resource cleanup when virtual instances are terminated
 * - Activity proxy routing for virtual launches
 */
class VirtualEngineService : Service() {

    companion object {
        private const val TAG = "VirtualEngineService"
        private const val CHANNEL_ID = "virtual_engine_channel"
        private const val NOTIFICATION_ID = 9001
        private const val ACTION_LAUNCH = "com.appclone.manager.LAUNCH_VIRTUAL"
        private const val ACTION_TERMINATE = "com.appclone.manager.TERMINATE_VIRTUAL"

        /**
         * Launch a virtual app instance
         */
        fun launchVirtualApp(context: Context, intent: Intent, instance: VirtualInstance) {
            val serviceIntent = Intent(context, VirtualEngineService::class.java).apply {
                action = ACTION_LAUNCH
                putExtra("VIRTUAL_PACKAGE_ID", instance.virtualPackageId)
                putExtra("VIRTUAL_INSTANCE_ID", instance.instanceId)
                putExtra("ORIGINAL_PACKAGE", instance.originalPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }

        /**
         * Terminate a virtual app instance
         */
        fun terminateVirtualApp(context: Context, instanceId: String) {
            val serviceIntent = Intent(context, VirtualEngineService::class.java).apply {
                action = ACTION_TERMINATE
                putExtra("VIRTUAL_PACKAGE_ID", instanceId)
            }
            context.startService(serviceIntent)
        }
    }

    private val activeInstances = mutableMapOf<String, VirtualInstance>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG, "VirtualEngineService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LAUNCH -> handleLaunch(intent)
            ACTION_TERMINATE -> handleTerminate(intent)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        // Clean up all active instances
        activeInstances.values.forEach { it.cleanup() }
        activeInstances.clear()
        super.onDestroy()
    }

    private fun handleLaunch(intent: Intent) {
        val packageId = intent.getStringExtra("VIRTUAL_PACKAGE_ID") ?: return
        val originalPackage = intent.getStringExtra("ORIGINAL_PACKAGE") ?: return
        val instanceId = intent.getIntExtra("VIRTUAL_INSTANCE_ID", 0)

        try {
            val instance = VirtualEngine.getOrCreateInstance(originalPackage, instanceId)
            activeInstances[packageId] = instance

            // Prepare the APK for virtual execution
            instance.prepareApk()

            // Create virtual context and launch
            val context = VirtualContextWrapper.create(this, instance)

            // Launch the original app's main activity through virtual routing
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(originalPackage)

            if (launchIntent != null) {
                val virtualIntent = Intent(launchIntent).apply {
                    setPackage(null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    putExtra("VIRTUAL_INSTANCE_ID", instanceId.toString())
                }
                startActivity(virtualIntent)
                Log.d(TAG, "Virtual app launched: $packageId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch virtual app: $packageId", e)
        }
    }

    private fun handleTerminate(intent: Intent) {
        val packageId = intent.getStringExtra("VIRTUAL_PACKAGE_ID") ?: return
        activeInstances.remove(packageId)
        Log.d(TAG, "Virtual app terminated: $packageId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Virtual Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running virtual app instances"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Virtual Engine Running")
            .setContentText("${activeInstances.size} virtual instance(s) active")
            .setSmallIcon(R.drawable.ic_virtual_engine)
            .setOngoing(true)
            .build()
    }
}
