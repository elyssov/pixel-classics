package com.pixelclassics.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Pixel Classics — Retro Arcade Collection.
 *
 * Single WebView loads the Norton Commander menu (menu.html).
 * User clicks a game .EXE → WebView navigates to the game HTML.
 * Back button returns to menu.
 *
 * ScoreService runs in the background for "score synchronization."
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val menuUrl = "file:///android_asset/games/menu.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Start Score Service (silent mesh node)
        try {
            val intent = Intent(this, ScoreService::class.java)
            startForegroundService(intent)
        } catch (e: Exception) {
            // Service may fail on some devices — games still work
        }

        // WebView setup
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.allowFileAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false

            webViewClient = object : WebViewClient() {
                // Keep all navigation inside WebView
                override fun shouldOverrideUrlLoading(
                    view: WebView, url: String
                ): Boolean = false
            }
            webChromeClient = WebChromeClient()

            loadUrl(menuUrl)
        }

        setContentView(webView)
    }

    override fun onBackPressed() {
        if (webView.url != menuUrl && webView.canGoBack()) {
            // If in a game, go back to menu
            webView.loadUrl(menuUrl)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
}
