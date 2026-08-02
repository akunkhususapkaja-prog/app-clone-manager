package com.appclone.manager.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.appclone.manager.engine.ApkInstaller
import com.appclone.manager.engine.VirtualEngine
import java.io.File

/**
 * ApkInstallActivity - Handles APK files opened from outside the app.
 * 
 * This activity is registered in the manifest to receive APK file intents,
 * allowing users to install APK files directly from file managers or
 * download managers into the virtual space.
 */
class ApkInstallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize virtual engine
        VirtualEngine.initialize(applicationContext)

        // Handle the APK intent
        val uri: Uri? = intent.data
        if (uri != null) {
            handleApkUri(uri)
        } else {
            Toast.makeText(this, "No APK file received", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri: Uri? = intent.data
        if (uri != null) {
            handleApkUri(uri)
        }
    }

    private fun handleApkUri(uri: Uri) {
        try {
            // Copy APK to our storage
            val fileName = "received_apk_${System.currentTimeMillis()}.apk"
            val destFile = File(getExternalFilesDir(null), fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Parse the APK
            val apkInfo = ApkInstaller.parseApk(destFile)
            if (apkInfo != null) {
                // Determine new instance ID
                val existingInstances = VirtualEngine.getInstancesForPackage(apkInfo.packageName)
                val newInstanceId = existingInstances.size + 1

                // Install to virtual space
                val success = ApkInstaller.installToVirtualInstance(
                    applicationContext,
                    destFile,
                    newInstanceId
                )

                Toast.makeText(
                    this,
                    if (success) {
                        "${apkInfo.appName} installed to virtual instance #$newInstanceId"
                    } else {
                        "Failed to install to virtual space"
                    },
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Could not parse APK file", Toast.LENGTH_SHORT).show()
            }

            // Clean up
            destFile.delete()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Navigate to main activity
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(mainIntent)
        finish()
    }
}
