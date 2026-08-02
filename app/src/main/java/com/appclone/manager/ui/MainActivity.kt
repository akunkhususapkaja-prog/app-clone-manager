package com.appclone.manager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.appclone.manager.ui.theme.AppCloneManagerTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appclone.manager.ui.screens.HomeScreen
import com.appclone.manager.ui.screens.CloneAppScreen
import com.appclone.manager.ui.screens.ApkInstallerScreen
import com.appclone.manager.ui.screens.PlayStoreScreen
import com.appclone.manager.ui.screens.ManagedAppsScreen
import com.appclone.manager.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCloneManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("clone") {
                            CloneAppScreen(navController)
                        }
                        composable("installer") {
                            ApkInstallerScreen(navController)
                        }
                        composable("playstore") {
                            PlayStoreScreen(navController)
                        }
                        composable("managed") {
                            ManagedAppsScreen(navController)
                        }
                        composable("settings") {
                            SettingsScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
