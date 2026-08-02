package com.appclone.manager.ui.screens

import android.content.pm.ApplicationInfo
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.engine.ApkInstaller
import com.appclone.manager.engine.VirtualEngine
import com.appclone.manager.engine.VirtualInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneAppScreen(navController: NavController) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var showCloneDialog by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf("") }
    var cloneMessage by remember { mutableStateOf<String?>(null) }

    // Get all installed packages (excluding system apps)
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
                title = { Text("Clone App") },
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
            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search installed apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Results count
            Text(
                text = "${filteredPackages.size} apps found",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Clone message
            if (cloneMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = cloneMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 13.sp
                    )
                }
            }

            // App list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPackages) { appInfo ->
                    val label = try {
                        appInfo.loadLabel(context.packageManager).toString()
                    } catch (e: Exception) {
                        appInfo.packageName
                    }

                    val icon = try {
                        appInfo.loadIcon(context.packageManager)
                    } catch (e: Exception) {
                        null
                    }

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

    // Clone dialog
    if (showCloneDialog) {
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            title = { Text("Clone App") },
            text = {
                Column {
                    Text("How many clones do you want to create for:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedPackage,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val success = ApkInstaller.cloneApp(context, selectedPackage, 1)
                    cloneMessage = if (success) {
                        "Clone created successfully!"
                    } else {
                        "Failed to create clone. Please try again."
                    }
                    showCloneDialog = false
                }) {
                    Text("Create Clone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneDialog = false }) {
                    Text("Cancel")
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            if (icon != null) {
                androidx.compose.foundation.Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Clone count badge
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Clone",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
