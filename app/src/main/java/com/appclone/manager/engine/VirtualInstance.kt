package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * VirtualInstance - Represents a single cloned instance of an application.
 */
data class VirtualInstance(
    val originalPackage: String,
    val instanceId: Int
) {
    companion object {
        private const val TAG = "VirtualInstance"
    }

    val virtualPackageId: String
        get() = "$originalPackage.clone.$instanceId"

    val virtualDataDir: File
        get() {
            val baseDir = VirtualEngine.getVirtualDataDir()
            val dir = File(baseDir, virtualPackageId)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    val virtualFilesDir: File
        get() = File(virtualDataDir, "files").apply { if (!exists()) mkdirs() }

    val virtualCacheDir: File
        get() = File(virtualDataDir, "cache").apply { if (!exists()) mkdirs() }

    val virtualDatabaseDir: File
        get() = File(virtualDataDir, "databases").apply { if (!exists()) mkdirs() }

    val virtualPreferencesDir: File
        get() = File(virtualDataDir, "shared_prefs").apply { if (!exists()) mkdirs() }

    val virtualNativeLibDir: File
        get() = File(virtualDataDir, "lib").apply { if (!exists()) mkdirs() }

    fun getClonedAppInfo(): android.content.pm.ApplicationInfo? {
        val context = VirtualEngine.getHostContext() ?: return null
        return try {
            context.packageManager.getApplicationInfo(originalPackage, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Original package not found: $originalPackage", e)
            null
        }
    }

    fun getAppLabel(): String {
        val info = getClonedAppInfo() ?: return originalPackage
        val context = VirtualEngine.getHostContext() ?: return originalPackage
        return info.loadLabel(context.packageManager).toString()
    }

    fun getAppIcon(): Drawable? {
        val info = getClonedAppInfo() ?: return null
        val context = VirtualEngine.getHostContext() ?: return null
        return try {
            info.loadIcon(context.packageManager)
        } catch (e: Exception) {
            null
        }
    }

    fun launch(): Boolean {
        val context = VirtualEngine.getHostContext() ?: return false
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(originalPackage)
                ?: return false

            val intent = Intent(launchIntent).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("VIRTUAL_INSTANCE_ID", instanceId.toString())
                putExtra("VIRTUAL_PACKAGE_ID", virtualPackageId)
                putExtra("VIRTUAL_DATA_DIR", virtualDataDir.absolutePath)
            }

            VirtualEngineService.launchVirtualApp(context, intent, this)
            Log.d(TAG, "Launched virtual instance: $virtualPackageId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch virtual instance: $virtualPackageId", e)
            false
        }
    }

    fun clearData(): Boolean {
        return virtualDataDir.deleteRecursively()
    }

    fun cleanup() {
        val dir = virtualDataDir
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        Log.d(TAG, "Cleaned up virtual instance: $virtualPackageId")
    }

    fun getDataSize(): Long {
        val dir = virtualDataDir
        if (!dir.exists()) return 0L
        return dir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VirtualInstance) return false
        return virtualPackageId == other.virtualPackageId
    }

    override fun hashCode(): Int = virtualPackageId.hashCode()

    override fun toString(): String = "VirtualInstance($virtualPackageId)"
}
