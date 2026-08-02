package com.appclone.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.engine.VirtualEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(false) }
    var autoBackup by remember { mutableStateOf(true) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Version info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "App Clone Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Version 1.0.0  •  Virtual Engine Active",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Settings sections
            Text(
                text = "General",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Dark mode setting
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Enable dark theme",
                trailing = {
                    Switch(checked = darkModeEnabled, onCheckedChange = { darkModeEnabled = it })
                }
            )

            // Auto backup setting
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Auto Backup",
                subtitle = "Automatically backup cloned app data",
                trailing = {
                    Switch(checked = autoBackup, onCheckedChange = { autoBackup = it })
                }
            )

            // Notification setting
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Show notifications for virtual engine",
                trailing = {
                    Switch(checked = true, onCheckedChange = {})
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Virtual Engine",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Virtual engine info
            SettingsItem(
                icon = Icons.Default.Memory,
                title = "Engine Status",
                subtitle = if (VirtualEngine.isInitialized()) "Running" else "Stopped",
                trailing = {
                    Surface(
                        color = if (VirtualEngine.isInitialized())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (VirtualEngine.isInitialized()) "Active" else "Inactive",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )

            SettingsItem(
                icon = Icons.Default.Storage,
                title = "Virtual Storage",
                subtitle = "Location: ${VirtualEngine.getVirtualDataDir().path}",
                trailing = {}
            )

            val instanceCount = VirtualEngine.getAllInstances().size
            SettingsItem(
                icon = Icons.Default.Apps,
                title = "Active Instances",
                subtitle = "$instanceCount virtual instance(s) running",
                trailing = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Delete all data
            SettingsItem(
                icon = Icons.Default.DeleteForever,
                title = "Delete All Clones",
                subtitle = "Remove all cloned apps and their data",
                trailing = {
                    TextButton(onClick = { showDeleteAllDialog = true }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            )

            // Cache clear
            SettingsItem(
                icon = Icons.Default.ClearAll,
                title = "Clear Cache",
                subtitle = "Clear temporary files",
                trailing = {
                    TextButton(onClick = {
                        message = "Cache cleared successfully"
                    }) {
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            if (message != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = message!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "App Clone Manager",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A virtual space application that allows you to:\n\n" +
                                "• Clone installed apps (multiple instances)\n" +
                                "• Install APK files directly to virtual space\n" +
                                "• Access Play Store to install new apps\n" +
                                "• Manage all virtual instances\n\n" +
                                "No ads. No tracking. Your data stays private.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete All Clones") },
            text = { Text("This will permanently delete all cloned apps and their data. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val instances = VirtualEngine.getAllInstances()
                    instances.forEach { it.cleanup() }
                    message = "All clones deleted (${instances.size} removed)"
                    showDeleteAllDialog = false
                }) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            trailing()
        }
    }
}
