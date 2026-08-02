package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * VirtualInstance - Represents a single cloned instance of an application.
 * 
 * Each instance has:
 * - Its own data directory (isolated from other instances)
 * - Its own virtual package ID for internal tracking
 * - Its own process isolation
 * - Ability to launch the cloned app independently
 * 
 * The virtual package ID format: original.package + ".clone." + instanceId
 * This allows the system to differentiate between instances.
 */
data class VirtualInstance(
    val originalPackage: String,
    val instanceId: Int
) {
    companion object {
        private const val TAG = "VirtualInstance"
        private const val DATA_SUFFIX = "_clone"
        private const val PREFERENCE_SUFFIX = "_clone_prefs"
    }

    /**
     * Unique virtual package identifier
     */
    val virtualPackageId: String
        get() = "$originalPackage.clone.$instanceId"

    /**
     * Virtual data directory for this instance
     */
    val virtualDataDir: File
        get() {
            val baseDir = VirtualEngine.getVirtualDataDir()
            return File(baseDir, virtualPackageId).apply {
                if (!exists()) mkdirs()
            }
        }

    /**
     * Virtual files directory
     */
    val virtualFilesDir: File
        get() = File(virtualDataDir, "files").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Virtual cache directory
     */
    val virtualCacheDir: File
        get() = File(virtualDataDir, "cache").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Virtual database directory
     */
    val virtualDatabaseDir: File
        get() = File(virtualDataDir, "databases").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Virtual shared preferences directory
     */
    val virtualPreferencesDir: File
        get() = File(virtualDataDir, "shared_prefs").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Virtual native library directory
     */
    val virtualNativeLibDir: File
        get() = File(virtualDataDir, "lib").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Get the cloned app info from the host system
     */
    fun getClonedAppInfo(): android.content.pm.ApplicationInfo? {
        val context = VirtualEngine.getHostContext() ?: return null
        return try {
            context.packageManager.getApplicationInfo(originalPackage, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Original package not found: $originalPackage", e)
            null
        }
    }

    /**
     * Get the app label/name
     */
    fun getAppLabel(): String {
        val info = getClonedAppInfo() ?: return originalPackage
        val context = VirtualEngine.getHostContext() ?: return originalPackage
        return info.loadLabel(context.packageManager).toString()
    }

    /**
     * Get the app icon as a Drawable
     */
    fun getAppIcon(): android.graphics.drawable.Drawable? {
        val info = getClonedAppInfo() ?: return null
        val context = VirtualEngine.getHostContext() ?: return null
        return try {
            info.loadIcon(context.packageManager)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Launch the cloned app instance
     * This creates an isolated environment and launches the app
     */
    fun launch(): Boolean {
        val context = VirtualEngine.getHostContext() ?: return false

        return try {
            // Get the launch intent for the original package
            val launchIntent = context.packageManager.getLaunchIntentForPackage(originalPackage)
                ?: return false

            // Create a virtual context that redirects data operations
            val virtualContext = createVirtualContext(context)

            // Launch the activity with virtual environment flags
            val intent = Intent(launchIntent).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("VIRTUAL_INSTANCE_ID", instanceId.toString())
                putExtra("VIRTUAL_PACKAGE_ID", virtualPackageId)
                putExtra("VIRTUAL_DATA_DIR", virtualDataDir.absolutePath)
            }

            // If we have a virtual engine service, route through it
            VirtualEngineService.launchVirtualApp(context, intent, this)
            Log.d(TAG, "Launched virtual instance: $virtualPackageId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch virtual instance: $virtualPackageId", e)
            false
        }
    }

    /**
     * Create a virtual context for this instance
     */
    private fun createVirtualContext(hostContext: Context): Context {
        // Create a new data directory for isolation
        return VirtualContextWrapper(hostContext, this)
    }

    /**
     * Copy the original app's APK to virtual storage
     */
    fun prepareApk(): Boolean {
        val context = VirtualEngine.getHostContext() ?: return false
        val appInfo = getClonedAppInfo() ?: return false

        return try {
            val sourceApk = File(appInfo.sourceDir)
            val destApk = File(virtualDataDir, "base.apk")

            if (!destApk.exists()) {
                sourceApk.inputStream().use { input ->
                    FileOutputStream(destApk).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare APK for: $originalPackage", e)
            false
        }
    }

    /**
     * Install an APK file into this virtual instance
     */
    fun installApk(apkFile: File): Boolean {
        return try {
            val destApk = File(virtualDataDir, "base.apk")
            apkFile.inputStream().use { input ->
                FileOutputStream(destApk).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "APK installed to virtual instance: $virtualPackageId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install APK to: $virtualPackageId", e)
            false
        }
    }

    /**
     * Get the size of virtual data
     */
    fun getDataSize(): Long {
        return virtualDataDir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    /**
     * Clear all data for this virtual instance
     */
    fun clearData(): Boolean {
        return virtualDataDir.deleteRecursively()
    }

    /**
     * Cleanup all resources for this instance
     */
    fun cleanup() {
        virtualDataDir.deleteRecursively()
        Log.d(TAG, "Cleaned up virtual instance: $virtualPackageId")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VirtualInstance) return false
        return virtualPackageId == other.virtualPackageId
    }

    override fun hashCode(): Int = virtualPackageId.hashCode()

    override fun toString(): String = "VirtualInstance($virtualPackageId)"
}
