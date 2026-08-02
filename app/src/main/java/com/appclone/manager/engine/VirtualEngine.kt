package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.UserManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * VirtualEngine - Core virtual space engine for multi-instance app cloning.
 * 
 * This engine manages a virtual Android environment that allows running
 * multiple instances of the same application. Each instance is isolated
 * with its own data directory, shared preferences, and database.
 * 
 * Architecture:
 * - Each virtual instance runs in a separate process namespace
 * - Data isolation via separate file directories
 * - Package name manipulation for system compatibility
 * - Activity launching through virtual intent routing
 */
object VirtualEngine {

    private const val TAG = "VirtualEngine"
    private const val VIRTUAL_DATA_DIR = "virtual_data"
    private const val VIRTUAL_APK_DIR = "virtual_apks"
    private const val VIRTUAL_PREFS_DIR = "virtual_prefs"

    // Singleton instance
    private var initialized = false
    private var hostContext: Context? = null

    // Track all virtual instances
    private val virtualInstances = mutableMapOf<String, VirtualInstance>()

    /**
     * Initialize the virtual engine
     */
    fun initialize(context: Context): Boolean {
        if (initialized) return true
        hostContext = context.applicationContext

        val dataDir = File(context.applicationInfo.dataDir, VIRTUAL_DATA_DIR)
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }

        val apkDir = File(dataDir, VIRTUAL_APK_DIR)
        if (!apkDir.exists()) {
            apkDir.mkdirs()
        }

        val prefsDir = File(dataDir, VIRTUAL_PREFS_DIR)
        if (!prefsDir.exists()) {
            prefsDir.mkdirs()
        }

        initialized = true
        Log.d(TAG, "VirtualEngine initialized successfully")
        return true
    }

    /**
     * Check if engine is initialized
     */
    fun isInitialized(): Boolean = initialized

    /**
     * Get the virtual data directory
     */
    fun getVirtualDataDir(): File {
        val context = hostContext ?: throw IllegalStateException("VirtualEngine not initialized")
        return File(context.applicationInfo.dataDir, VIRTUAL_DATA_DIR)
    }

    /**
     * Get or create a virtual instance for an app
     * 
     * @param originalPackage original package name
     * @param instanceId unique identifier for this clone instance
     * @return VirtualInstance representing the cloned app
     */
    fun getOrCreateInstance(originalPackage: String, instanceId: Int): VirtualInstance {
        val key = "$originalPackage:$instanceId"
        return virtualInstances.getOrPut(key) {
            VirtualInstance(originalPackage, instanceId)
        }
    }

    /**
     * Get all virtual instances
     */
    fun getAllInstances(): List<VirtualInstance> {
        return virtualInstances.values.toList()
    }

    /**
     * Get instances of a specific package
     */
    fun getInstancesForPackage(packageName: String): List<VirtualInstance> {
        return virtualInstances.values.filter { it.originalPackage == packageName }
    }

    /**
     * Remove a virtual instance
     */
    fun removeInstance(instanceId: String): Boolean {
        val instance = virtualInstances.remove(instanceId) ?: return false
        instance.cleanup()
        return true
    }

    /**
     * Check if a package exists in the host system
     */
    fun isPackageInstalled(packageName: String): Boolean {
        val context = hostContext ?: return false
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get application info for a package
     */
    fun getApplicationInfo(packageName: String): ApplicationInfo? {
        val context = hostContext ?: return null
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Get all installed packages
     */
    fun getInstalledPackages(): List<ApplicationInfo> {
        val context = hostContext ?: return emptyList()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(0)
            )
        } else {
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
    }

    /**
     * Get the host context
     */
    fun getHostContext(): Context? = hostContext
}
