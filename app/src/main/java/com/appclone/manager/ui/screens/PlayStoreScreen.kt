package com.appclone.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appclone.manager.R
import com.appclone.manager.engine.PlayStoreIntegration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreScreen(navController: NavController) {
    val context = LocalContext.current
    var isCloned by remember { mutableStateOf(PlayStoreIntegration.isPlayStoreCloned()) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_play_store)) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Store,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isCloned) {
                Text("Play Store is ready in virtual space", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { PlayStoreIntegration.openPlayStoreForApp(context, "") }) {
                    Text("Open Play Store")
                }
            } else {
                Text("Play Store is not yet in virtual space", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Clone the system Play Store to install apps directly inside clones.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    if (PlayStoreIntegration.clonePlayStore(context)) {
                        isCloned = true
                        message = "Play Store cloned successfully!"
                    } else {
                        message = "Failed to clone Play Store."
                    }
                }) {
                    Text("Clone Play Store")
                }
            }

            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(message!!, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
