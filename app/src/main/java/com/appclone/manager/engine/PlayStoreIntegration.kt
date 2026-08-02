package com.appclone.manager.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * PlayStoreIntegration - Provides Play Store integration within the virtual space.
 * 
 * This class handles:
 * - Opening Play Store deep links that install to virtual space
 * - Searching apps on Play Store
 * - Managing Play Store access within the virtual environment
 * 
 * Note: Due to Google Play restrictions, we use deep links to open
 * the Play Store app. The virtual engine routes the installation
 * to the appropriate virtual instance.
 */
object PlayStoreIntegration {

    private const val TAG = "PlayStoreIntegration"
    private const val PLAY_STORE_PACKAGE = "com.android.vending"
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="
    private const val MARKET_URL = "market://details?id="

    /**
     * Open Play Store for a specific app
     * Uses deep link to open Play Store app directly (not WebView)
     * 
     * @param context Android context
     * @param packageName Package name to open in Play Store
     */
    fun openPlayStoreForApp(context: Context, packageName: String) {
        try {
            // Try market:// deep link first (opens Play Store app)
            val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("$MARKET_URL$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if Play Store is available
            if (isPlayStoreAvailable(context)) {
                context.startActivity(marketIntent)
                Log.d(TAG, "Opened Play Store for: $packageName")
            } else {
                // Fallback to web URL
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("$PLAY_STORE_URL$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                Log.d(TAG, "Play Store not available, opened web fallback for: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Play Store", e)
        }
    }

    /**
     * Open Play Store search
     */
    fun searchPlayStore(context: Context, query: String) {
        try {
            val searchIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?q=$query")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search Play Store", e)
        }
    }

    /**
     * Open Play Store category
     */
    fun openPlayStoreCategory(context: Context, category: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?c=apps&q=$category")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open category", e)
        }
    }

    /**
     * Open Play Store featured/top charts
     */
    fun openPlayStoreTopCharts(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.android.vending")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Play Store", e)
        }
    }

    /**
     * Check if Play Store is installed on the device
     */
    fun isPlayStoreAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(PLAY_STORE_PACKAGE, 0)
            true
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get the Play Store package name (in case it varies by device)
     */
    fun getPlayStorePackage(context: Context): String {
        val alternatives = listOf(
            "com.android.vending",
            "com.google.android.finsky",
            "com.huawei.appmarket",
            "com.sec.android.app.samsungapps"
        )

        return alternatives.firstOrNull { packageName ->
            try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        } ?: PLAY_STORE_PACKAGE
    }

    /**
     * Embed Play Store in a virtual container view
     * This creates a WebView-based Play Store view within the virtual space
     */
    fun createPlayStoreContainer(context: Context): PlayStoreContainer {
        return PlayStoreContainer(context)
    }
}

/**
 * Container that embeds Play Store functionality within the app
 */
class PlayStoreContainer(private val context: Context) {

    private val webView: android.webkit.WebView

    init {
        webView = android.webkit.WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                builtInZoomControls = false
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
            }
        }
    }

    /**
     * Load Play Store search results
     */
    fun loadSearch(query: String) {
        webView.loadUrl("https://play.google.com/store/search?q=$query&c=apps")
    }

    /**
     * Load specific app page
     */
    fun loadAppDetail(packageName: String) {
        webView.loadUrl("https://play.google.com/store/apps/details?id=$packageName")
    }

    /**
     * Get the WebView to embed in the UI
     */
    fun getWebView(): android.webkit.WebView = webView

    /**
     * Clean up resources
     */
    fun destroy() {
        webView.destroy()
    }
}
