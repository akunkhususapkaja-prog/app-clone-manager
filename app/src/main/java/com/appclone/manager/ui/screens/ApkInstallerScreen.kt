package com.appclone.manager.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.engine.ApkInstaller
import com.appclone.manager.engine.VirtualEngine
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkInstallerScreen(navController: NavController) {
    val context = LocalContext.current
    var apkFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var installMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load APK files on composition
    LaunchedEffect(Unit) {
        apkFiles = ApkInstaller.getApkFiles(context)
        isLoading = false
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val fileName = "custom_apk_${System.currentTimeMillis()}.apk"
                    val destFile = File(context.getExternalFilesDir(null), fileName)
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    // Parse and install
                    val apkInfo = ApkInstaller.parseApk(destFile)
                    if (apkInfo != null) {
                        val newInstanceId = VirtualEngine.getInstancesForPackage(apkInfo.packageName).size + 1
                        val success = ApkInstaller.installToVirtualInstance(
                            context, destFile, newInstanceId
                        )
                        installMessage = if (success) {
                            "APK installed successfully to virtual instance #${newInstanceId}"
                        } else {
                            "Failed to install APK"
                        }
                    } else {
                        installMessage = "Could not parse APK file"
                    }
                }
                // Refresh file list
                apkFiles = ApkInstaller.getApkFiles(context)
            } catch (e: Exception) {
                installMessage = "Error: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APK Installer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { filePickerLauncher.launch(arrayOf("application/vnd.android.package-archive")) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add APK")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch(arrayOf("application/vnd.android.package-archive")) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Select APK")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Status message
            if (installMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = if (installMessage!!.contains("successfully"))
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = installMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = if (installMessage!!.contains("successfully"))
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }

            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to install APK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Place APK files in Downloads folder\n2. Or tap + button to select APK file\n3. APK will be installed to virtual space",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (apkFiles.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No APK files found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap + to select an APK file",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // APK files list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apkFiles) { file ->
                        ApkFileCard(
                            file = file,
                            onInstall = {
                                val apkInfo = ApkInstaller.parseApk(file)
                                if (apkInfo != null) {
                                    val newInstanceId = VirtualEngine.getInstancesForPackage(apkInfo.packageName).size + 1
                                    val success = ApkInstaller.installToVirtualInstance(
                                        context, file, newInstanceId
                                    )
                                    installMessage = if (success) {
                                        "Installed ${apkInfo.appName} to virtual instance #${newInstanceId}"
                                    } else {
                                        "Failed to install ${apkInfo.appName}"
                                    }
                                } else {
                                    installMessage = "Could not parse: ${file.name}"
                                }
                            },
                            onInstallToSystem = {
                                val success = ApkInstaller.installToSystem(context, file)
                                if (!success) {
                                    installMessage = "Could not launch installer"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApkFileCard(
    file: File,
    onInstall: () -> Unit,
    onInstallToSystem: () -> Unit
) {
    val apkInfo = remember(file) { ApkInstaller.parseApk(file) }
    val fileSize = remember(file) {
        val bytes = file.length()
        when {
            bytes >= 1_048_576 -> "${String.format("%.1f", bytes / 1_048_576.0)} MB"
            bytes >= 1024 -> "${String.format("%.1f", bytes / 1024.0)} KB"
            else -> "${bytes} B"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = apkInfo?.appName ?: file.nameWithoutExtension,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = file.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$fileSize  •  v${apkInfo?.versionName ?: "?"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onInstallToSystem,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("System", fontSize = 12.sp)
                }

                Button(
                    onClick = onInstall,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.InstallDesktop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Install to Virtual", fontSize = 12.sp)
                }
            }
        }
    }
}
