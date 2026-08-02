package com.appclone.manager.ui.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.appclone.manager.R
import com.appclone.manager.engine.ApkInstaller
import com.appclone.manager.engine.VirtualEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneAppScreen(navController: NavController) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var showCloneDialog by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf("") }
    var cloneMessage by remember { mutableStateOf<String?>(null) }

    val allPackages = remember {
        VirtualEngine.getInstalledPackages().filter { appInfo ->
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
            appInfo.packageName != context.packageName
        }
    }

    val filteredPackages = remember(searchText) {
        if (searchText.isEmpty()) {
            allPackages
        } else {
            allPackages.filter { appInfo ->
                val label = try {
                    appInfo.loadLabel(context.packageManager).toString()
                } catch (e: Exception) {
                    appInfo.packageName
                }
                label.contains(searchText, ignoreCase = true) ||
                appInfo.packageName.contains(searchText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_clone_app)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (cloneMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(vertical = 8.dp),
                    action = {
                        TextButton(onClick = { cloneMessage = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(cloneMessage!!)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredPackages) { appInfo ->
                    val label = appInfo.loadLabel(context.packageManager).toString()
                    val icon = appInfo.loadIcon(context.packageManager)
                    val instanceCount = VirtualEngine.getInstancesForPackage(appInfo.packageName).size

                    AppItemCard(
                        appName = label,
                        packageName = appInfo.packageName,
                        instanceCount = instanceCount,
                        icon = icon,
                        onClick = {
                            selectedPackage = appInfo.packageName
                            showCloneDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showCloneDialog) {
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            title = { Text(stringResource(R.string.action_clone_app)) },
            text = { Text("Create a new clone for $selectedPackage?") },
            confirmButton = {
                TextButton(onClick = {
                    val success = ApkInstaller.cloneApp(context, selectedPackage, 1)
                    cloneMessage = if (success) "Clone created!" else "Failed to clone."
                    showCloneDialog = false
                }) {
                    Text("Clone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun AppItemCard(
    appName: String,
    packageName: String,
    instanceCount: Int,
    icon: android.graphics.drawable.Drawable?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.padding(10.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(appName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(packageName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (instanceCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "$instanceCount",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
