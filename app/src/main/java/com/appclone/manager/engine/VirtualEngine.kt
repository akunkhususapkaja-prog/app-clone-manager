package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
 */
object VirtualEngine {

    private const val TAG = "VirtualEngine"
    private const val VIRTUAL_DATA_DIR = "virtual_data"
    private const val VIRTUAL_APK_DIR = "virtual_apks"
    private const val VIRTUAL_PREFS_DIR = "virtual_prefs"
    private const val PREFS_NAME = "virtual_engine_prefs"
    private const val KEY_INSTANCES = "cloned_instances"

    private var initialized = false
    private var hostContext: Context? = null
    private val virtualInstances = mutableMapOf<String, VirtualInstance>()
    private lateinit var prefs: SharedPreferences

    /**
     * Initialize the virtual engine
     */
    fun initialize(context: Context): Boolean {
        if (initialized) return true
        hostContext = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

        loadInstances()
        initialized = true
        Log.d(TAG, "VirtualEngine initialized successfully")
        return true
    }

    private fun loadInstances() {
        val savedInstances = prefs.getStringSet(KEY_INSTANCES, emptySet()) ?: emptySet()
        virtualInstances.clear()
        savedInstances.forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val pkg = parts[0]
                val id = parts[1].toIntOrNull() ?: return@forEach
                val instance = VirtualInstance(pkg, id)
                virtualInstances["$pkg:$id"] = instance
            }
        }
    }

    private fun saveInstances() {
        val set = virtualInstances.keys.toSet()
        prefs.edit().putStringSet(KEY_INSTANCES, set).apply()
    }

    fun isInitialized(): Boolean = initialized

    fun getVirtualDataDir(): File {
        val context = hostContext ?: throw IllegalStateException("VirtualEngine not initialized")
        return File(context.applicationInfo.dataDir, VIRTUAL_DATA_DIR)
    }

    fun getOrCreateInstance(originalPackage: String, instanceId: Int): VirtualInstance {
        val key = "$originalPackage:$instanceId"
        val instance = virtualInstances.getOrPut(key) {
            VirtualInstance(originalPackage, instanceId)
        }
        saveInstances()
        return instance
    }

    fun getAllInstances(): List<VirtualInstance> {
        return virtualInstances.values.toList()
    }

    fun getInstancesForPackage(packageName: String): List<VirtualInstance> {
        return virtualInstances.values.filter { it.originalPackage == packageName }
    }

    /**
     * Remove a virtual instance by its unique key (package:id) or virtualPackageId (package.clone.id)
     */
    fun removeInstance(identifier: String): Boolean {
        // Try to find the instance by either key format
        val key = if (identifier.contains(".clone.")) {
            val parts = identifier.split(".clone.")
            if (parts.size == 2) "${parts[0]}:${parts[1]}" else identifier
        } else {
            identifier
        }

        val instance = virtualInstances.remove(key) ?: return false
        instance.cleanup()
        saveInstances()
        return true
    }

    fun removeAllInstances(): Boolean {
        val instances = virtualInstances.values.toList()
        instances.forEach { it.cleanup() }
        virtualInstances.clear()
        saveInstances()
        return true
    }

    fun clearAllCache(): Boolean {
        val dataDir = getVirtualDataDir()
        return dataDir.walkTopDown().forEach { file ->
            if (file.name == "cache" && file.isDirectory) {
                file.deleteRecursively()
            }
        } != Unit
    }

    fun isPackageInstalled(packageName: String): Boolean {
        val context = hostContext ?: return false
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getApplicationInfo(packageName: String): ApplicationInfo? {
        val context = hostContext ?: return null
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

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

    fun getHostContext(): Context? = hostContext
}
