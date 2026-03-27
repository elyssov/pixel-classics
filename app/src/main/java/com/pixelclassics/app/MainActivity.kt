package com.pixelclassics.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val menuUrl = "file:///android_asset/games/menu.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Start Score Service with delay (don't block WebView)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                startForegroundService(Intent(this, ScoreService::class.java))
            } catch (e: Exception) {
                Log.e("PixelClassics", "ScoreService: ${e.message}")
            }
        }, 5000)

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
                override fun shouldOverrideUrlLoading(
                    view: WebView, url: String
                ): Boolean = false
            }
            webChromeClient = WebChromeClient()

            loadUrl(menuUrl)
        }

        setContentView(webView)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (webView.url != menuUrl && webView.canGoBack()) {
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
