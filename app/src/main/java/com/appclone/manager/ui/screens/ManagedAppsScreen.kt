package com.appclone.manager.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.engine.ApkInstaller
import com.appclone.manager.engine.VirtualEngine
import com.appclone.manager.engine.VirtualInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedAppsScreen(navController: NavController) {
    val context = LocalContext.current
    var instances by remember { mutableStateOf(VirtualEngine.getAllInstances()) }
    var showDeleteDialog by remember { mutableStateOf<VirtualInstance?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    // Refresh instances periodically
    LaunchedEffect(Unit) {
        instances = VirtualEngine.getAllInstances()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Managed Apps") },
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
            // Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${instances.size} instances",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { instances = VirtualEngine.getAllInstances() },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", fontSize = 12.sp)
                }
            }

            if (message != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = message!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 13.sp
                    )
                }
            }

            if (instances.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No cloned apps yet",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Clone an app from the Clone App section",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instances) { instance ->
                        ManagedAppCard(
                            instance = instance,
                            onLaunch = {
                                instance.launch()
                                message = "Launching ${instance.getAppLabel()}..."
                            },
                            onDelete = {
                                showDeleteDialog = instance
                            },
                            onClearData = {
                                val success = instance.clearData()
                                message = if (success) "Data cleared" else "Failed to clear data"
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { instance ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Clone") },
            text = {
                Text("Are you sure you want to delete the clone of ${instance.getAppLabel()}? All data will be lost.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val success = ApkInstaller.uninstallVirtualInstance(instance.virtualPackageId)
                    instances = VirtualEngine.getAllInstances()
                    message = if (success) "Clone deleted successfully" else "Failed to delete clone"
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ManagedAppCard(
    instance: VirtualInstance,
    onLaunch: () -> Unit,
    onDelete: () -> Unit,
    onClearData: () -> Unit
) {
    val dataSize = remember(instance) {
        val bytes = instance.getDataSize()
        when {
            bytes >= 1_048_576 -> "${String.format("%.1f", bytes / 1_048_576.0)} MB"
            bytes >= 1024 -> "${String.format("%.1f", bytes / 1024.0)} KB"
            else -> "${bytes} B"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLaunch),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = instance.getAppLabel(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "Clone #${instance.instanceId}  •  $dataSize",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onClearData,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ClearAll,
                        contentDescription = "Clear data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
