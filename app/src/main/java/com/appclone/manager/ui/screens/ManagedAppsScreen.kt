package com.appclone.manager.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.R
import com.appclone.manager.engine.VirtualEngine
import com.appclone.manager.engine.VirtualInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedAppsScreen(navController: NavController) {
    var instances by remember { mutableStateOf(VirtualEngine.getAllInstances()) }
    var showDeleteDialog by remember { mutableStateOf<VirtualInstance?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        instances = VirtualEngine.getAllInstances()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_managed_apps)) },
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
            if (message != null) {
                Snackbar(
                    modifier = Modifier.padding(vertical = 8.dp),
                    action = {
                        TextButton(onClick = { message = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message!!)
                }
            }

            if (instances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_clones_yet))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(instances) { instance ->
                        ManagedAppCard(
                            instance = instance,
                            onLaunch = { instance.launch() },
                            onDelete = { showDeleteDialog = instance },
                            onClearData = {
                                if (instance.clearData()) {
                                    message = "Data cleared"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { instance ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.dialog_delete)) },
            text = { Text("Delete clone of ${instance.getAppLabel()}?") },
            confirmButton = {
                TextButton(onClick = {
                    if (VirtualEngine.removeInstance(instance.virtualPackageId)) {
                        instances = VirtualEngine.getAllInstances()
                        message = "Clone deleted"
                    }
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
fun ManagedAppCard(
    instance: VirtualInstance,
    onLaunch: () -> Unit,
    onDelete: () -> Unit,
    onClearData: () -> Unit
) {
    val icon = remember(instance) { instance.getAppIcon() }
    val imageBitmap = remember(icon) {
        icon?.let {
            try {
                val width = it.intrinsicWidth.takeIf { w -> w > 0 } ?: 100
                val height = it.intrinsicHeight.takeIf { h -> h > 0 } ?: 100
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
                bitmap.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    val dataSize = remember(instance) {
        val bytes = instance.getDataSize()
        when {
            bytes >= 1_048_576 -> "${String.format("%.1f", bytes / 1_048_576.0)} MB"
            bytes >= 1024 -> "${String.format("%.1f", bytes / 1024.0)} KB"
            else -> "${bytes} B"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onLaunch),
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
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
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
                Text(instance.getAppLabel(), fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.clone_number, instance.instanceId) + " • $dataSize",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClearData) {
                Icon(Icons.Default.CleaningServices, contentDescription = "Clear Data", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}
