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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.R
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

    LaunchedEffect(Unit) {
        apkFiles = ApkInstaller.getApkFiles(context)
        isLoading = false
    }

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
                    val apkInfo = ApkInstaller.parseApk(destFile)
                    if (apkInfo != null) {
                        val newInstanceId = VirtualEngine.getInstancesForPackage(apkInfo.packageName).size + 1
                        val success = ApkInstaller.installToVirtualInstance(context, destFile, newInstanceId)
                        installMessage = if (success) "Installed to virtual space!" else "Failed to install."
                    }
                }
                apkFiles = ApkInstaller.getApkFiles(context)
            } catch (e: Exception) {
                installMessage = "Error: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_install_apk)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch(arrayOf("application/vnd.android.package-archive")) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add APK")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (installMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(vertical = 8.dp),
                    action = {
                        TextButton(onClick = { installMessage = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(installMessage!!)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (apkFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No APK files found. Tap + to add.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(apkFiles) { file ->
                        ApkFileCard(
                            file = file,
                            onInstall = {
                                val apkInfo = ApkInstaller.parseApk(file)
                                if (apkInfo != null) {
                                    val newInstanceId = VirtualEngine.getInstancesForPackage(apkInfo.packageName).size + 1
                                    val success = ApkInstaller.installToVirtualInstance(context, file, newInstanceId)
                                    installMessage = if (success) "Installed!" else "Failed."
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
private fun ApkFileCard(file: File, onInstall: () -> Unit) {
    val apkInfo = remember(file) { ApkInstaller.parseApk(file) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(apkInfo?.appName ?: file.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(file.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onInstall, shape = RoundedCornerShape(8.dp)) {
                Text("Install", fontSize = 12.sp)
            }
        }
    }
}
