package com.appclone.manager.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.appclone.manager.engine.PlayStoreIntegration
import com.appclone.manager.engine.VirtualEngine
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Featured") }
    var showDirectPlayStore by remember { mutableStateOf(true) }

    val categories = listOf(
        "Featured" to Icons.Default.Star,
        "Top Charts" to Icons.Default.TrendingUp,
        "Categories" to Icons.Default.Category,
        "Editor Choice" to Icons.Default.Work
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play Store")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDirectPlayStore = !showDirectPlayStore }) {
                        Icon(
                            if (showDirectPlayStore) Icons.Default.OpenInBrowser else Icons.Default.Web,
                            contentDescription = if (showDirectPlayStore) "Open in Play Store app" else "Open embedded"
                        )
                    }
                    IconButton(onClick = {
                        PlayStoreIntegration.openPlayStoreForApp(context, "com.google.android.apps.docs")
                    }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open Play Store")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showDirectPlayStore) {
                            "Tap on any section to open Play Store app directly. Apps installed will be available in virtual space."
                        } else {
                            "Browsing Play Store in embedded mode. Install button will open Play Store app."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (VirtualEngine.isInitialized()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Virtual Engine Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
                modifier = Modifier.padding(horizontal = 8.dp),
                edgePadding = 8.dp
            ) {
                categories.forEach { (name, icon) ->
                    Tab(
                        selected = selectedCategory == name,
                        onClick = {
                            selectedCategory = name
                            showDirectPlayStore = true
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedCategory) {
                "Featured" -> FeaturedSection(context, showDirectPlayStore)
                "Top Charts" -> TopChartsSection(context)
                "Categories" -> CategoriesSection(context)
                "Editor Choice" -> EditorsChoiceSection(context)
            }
        }
    }
}

@Composable
private fun FeaturedSection(context: android.content.Context, useDirectLink: Boolean) {
    val featuredApps = listOf(
        "WhatsApp" to "com.whatsapp",
        "Instagram" to "com.instagram.android",
        "Facebook" to "com.facebook.katana",
        "TikTok" to "com.zhiliaoapp.musically",
        "Telegram" to "org.telegram.messenger",
        "Spotify" to "com.spotify.music",
        "Netflix" to "com.netflix.mediaclient",
        "Google Maps" to "com.google.android.apps.maps",
        "YouTube" to "com.google.android.youtube",
        "Twitter" to "com.twitter.android"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Featured Apps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(featuredApps) { (name, packageName) ->
            FeaturedAppItem(
                name = name,
                packageName = packageName,
                context = context
            )
        }
    }
}

@Composable
private fun FeaturedAppItem(
    name: String,
    packageName: String,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colors = listOf(
                Color(0xFF25D366),
                Color(0xFFE1306C),
                Color(0xFF1877F2),
                Color(0xFF000000),
                Color(0xFF0088CC),
                Color(0xFF1DB954),
                Color(0xFFE50914),
                Color(0xFF4285F4),
                Color(0xFFFF0000),
                Color(0xFF1DA1F2)
            )
            val colorIndex = abs(name.hashCode()) % colors.size
            val iconColor = colors[colorIndex]

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    PlayStoreIntegration.openPlayStoreForApp(context, packageName)
                },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    Icons.Default.InstallDesktop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Install", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TopChartsSection(context: android.content.Context) {
    val topApps = listOf(
        "WhatsApp" to "com.whatsapp",
        "Facebook" to "com.facebook.katana",
        "Instagram" to "com.instagram.android",
        "TikTok" to "com.zhiliaoapp.musically",
        "Messenger" to "com.facebook.orca",
        "Telegram" to "org.telegram.messenger",
        "Snapchat" to "com.snapchat.android",
        "Google" to "com.google.android.googlequicksearchbox",
        "Spotify" to "com.spotify.music",
        "Netflix" to "com.netflix.mediaclient"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Top Charts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(topApps) { (name, packageName) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${topApps.indexOf(name to packageName) + 1}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = name,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        PlayStoreIntegration.openPlayStoreForApp(context, packageName)
                    }
                ) {
                    Text("Install", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CategoriesSection(context: android.content.Context) {
    val categories = listOf(
        "Games" to Icons.Default.SportsEsports,
        "Social" to Icons.Default.People,
        "Tools" to Icons.Default.Build,
        "Photography" to Icons.Default.PhotoCamera,
        "Music & Audio" to Icons.Default.MusicNote,
        "Video Players" to Icons.Default.PlayCircle,
        "Communication" to Icons.Default.Chat,
        "Entertainment" to Icons.Default.Movie,
        "Productivity" to Icons.Default.Work,
        "Education" to Icons.Default.School,
        "Health & Fitness" to Icons.Default.Favorite,
        "Shopping" to Icons.Default.ShoppingBag,
        "News" to Icons.Default.Newspaper,
        "Travel" to Icons.Default.Flight,
        "Finance" to Icons.Default.AccountBalance
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(categories) { (name, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorsChoiceSection(context: android.content.Context) {
    val editorsChoice = listOf(
        "Notion" to "notion.id",
        "Figma" to "com.figma.mirror",
        "Canva" to "com.canva.editor",
        "Adobe Lightroom" to "com.adobe.lrmobile",
        "Google Keep" to "com.google.android.keep",
        "Evernote" to "com.evernote",
        "Pocket" to "com.ideashower.readitlater.pro",
        "Pocket Casts" to "au.com.shiftyjelly.pocketcasts"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Editor Choice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(editorsChoice) { (name, packageName) ->
            FeaturedAppItem(name, packageName, context)
        }
    }
}
