package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.io.FileOutputStream

/**
 * ApkInstaller - Handles APK installation into the virtual space.
 * 
 * Features:
 * - Parse APK metadata (name, icon, package info)
 * - Copy APK to virtual storage
 * - Install APK via system PackageInstaller
 * - Support for installing into specific virtual instances
 */
object ApkInstaller {

    private const val TAG = "ApkInstaller"

    /**
     * APK metadata extracted from the APK file
     */
    data class ApkInfo(
        val packageName: String,
        val appName: String,
        val versionName: String,
        val versionCode: Long,
        val iconUri: String?,
        val fileSize: Long
    )

    /**
     * Parse APK file and extract metadata
     */
    fun parseApk(apkFile: File): ApkInfo? {
        return try {
            val apkParser = ApkFile(apkFile)
            val metaInfo = apkParser.metaInfo
            val manifest = apkParser.apkMeta

            ApkInfo(
                packageName = manifest.packageName,
                appName = manifest.name ?: manifest.packageName,
                versionName = manifest.versionName ?: "1.0",
                versionCode = manifest.versionCode ?: 0L,
                iconUri = null, // Icon extraction is complex
                fileSize = apkFile.length()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse APK: ${apkFile.name}", e)
            null
        }
    }

    /**
     * Get all APK files from storage
     */
    fun getApkFiles(context: Context): List<File> {
        val apkFiles = mutableListOf<File>()

        // Check Downloads folder
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        if (downloadsDir.exists()) {
            downloadsDir.listFiles { file -> file.extension.equals("apk", ignoreCase = true) }
                ?.let { apkFiles.addAll(it) }
        }

        // Check app's external files directory
        val appFilesDir = context.getExternalFilesDir(null)
        if (appFilesDir?.exists() == true) {
            appFilesDir.listFiles { file -> file.extension.equals("apk", ignoreCase = true) }
                ?.let { apkFiles.addAll(it) }
        }

        return apkFiles.sortedByDescending { it.lastModified() }
    }

    /**
     * Install APK to a specific virtual instance
     */
    fun installToVirtualInstance(context: Context, apkFile: File, instanceId: Int): Boolean {
        val apkInfo = parseApk(apkFile) ?: return false

        // Create or get the virtual instance
        val instance = VirtualEngine.getOrCreateInstance(apkInfo.packageName, instanceId)

        return try {
            // Copy APK to virtual storage
            val destApk = File(instance.virtualDataDir, "base.apk")
            apkFile.inputStream().use { input ->
                FileOutputStream(destApk).use { output ->
                    input.copyTo(output)
                }
            }

            // Also copy split APKs if they exist
            val splitsDir = File(instance.virtualDataDir, "splits")
            if (!splitsDir.exists()) splitsDir.mkdirs()

            Log.d(TAG, "APK installed to virtual instance: ${instance.virtualPackageId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install APK to virtual instance", e)
            false
        }
    }

    /**
     * Launch the system APK installer (for installing to the main system)
     */
    fun installToSystem(context: Context, apkFile: File): Boolean {
        return try {
            val apkUri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                apkUri = Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch system installer", e)
            false
        }
    }

    /**
     * Clone an installed app to a new virtual instance
     */
    fun cloneApp(context: Context, packageName: String, newInstanceId: Int): Boolean {
        val appInfo = VirtualEngine.getApplicationInfo(packageName) ?: return false

        // Get all existing instances for this package
        val existingInstances = VirtualEngine.getInstancesForPackage(packageName)
        val newId = if (existingInstances.isEmpty()) {
            newInstanceId
        } else {
            existingInstances.maxOf { it.instanceId } + 1
        }

        // Create new virtual instance
        val newInstance = VirtualEngine.getOrCreateInstance(packageName, newId)

        // Copy the APK from installed location
        val sourceApk = File(appInfo.sourceDir)
        val destApk = File(newInstance.virtualDataDir, "base.apk")

        return try {
            sourceApk.inputStream().use { input ->
                FileOutputStream(destApk).use { output ->
                    input.copyTo(output)
                }
            }

            // Copy native libraries
            val nativeLibsDir = File(appInfo.nativeLibraryDir)
            if (nativeLibsDir.exists()) {
                val destLibsDir = newInstance.virtualNativeLibDir
                nativeLibsDir.listFiles()?.forEach { libFile ->
                    val destLib = File(destLibsDir, libFile.name)
                    libFile.inputStream().use { input ->
                        FileOutputStream(destLib).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            Log.d(TAG, "App cloned successfully: ${newInstance.virtualPackageId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clone app: $packageName", e)
            false
        }
    }

    /**
     * Uninstall a virtual instance
     */
    fun uninstallVirtualInstance(instanceId: String): Boolean {
        return VirtualEngine.removeInstance(instanceId)
    }
}
