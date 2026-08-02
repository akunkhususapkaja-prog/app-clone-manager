package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * PlayStoreIntegration - Handles Play Store integration within the virtual space.
 */
object PlayStoreIntegration {

    private const val TAG = "PlayStoreIntegration"
    private const val PLAY_STORE_PACKAGE = "com.android.vending"

    /**
     * Attempts to open the Play Store for a specific package.
     * If a virtual instance of Play Store exists, it will try to use it.
     */
    fun openPlayStoreForApp(context: Context, packageName: String) {
        val playStoreInstances = VirtualEngine.getInstancesForPackage(PLAY_STORE_PACKAGE)
        
        if (playStoreInstances.isNotEmpty()) {
            // Launch the virtual Play Store instance
            val intent = context.packageManager.getLaunchIntentForPackage(PLAY_STORE_PACKAGE)
            if (intent != null) {
                intent.data = Uri.parse("market://details?id=$packageName")
                playStoreInstances.first().launch() // This is a simplification
                return
            }
        }

        // Fallback to host Play Store
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Clones the system Play Store into the virtual environment.
     */
    fun clonePlayStore(context: Context): Boolean {
        return ApkInstaller.cloneApp(context, PLAY_STORE_PACKAGE, 1)
    }

    fun isPlayStoreCloned(): Boolean {
        return VirtualEngine.getInstancesForPackage(PLAY_STORE_PACKAGE).isNotEmpty()
    }
}
