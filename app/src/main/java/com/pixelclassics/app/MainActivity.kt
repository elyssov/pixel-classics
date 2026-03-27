package com.pixelclassics.app

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val menuUrl = "file:///android_asset/games/menu.html"
    private val TAG = "PixelClassics"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fullscreen error: ${e.message}")
        }

        // Start Score Service in background (don't let it block app start)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val intent = Intent(this, ScoreService::class.java)
                startForegroundService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "ScoreService start failed: ${e.message}")
            }
        }, 3000) // 3 second delay — let WebView load first

        // WebView setup
        webView = WebView(this).apply {
            setBackgroundColor(0xFF000000.toInt()) // Black while loading

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            // Samsung WebView sometimes needs these
            settings.databaseEnabled = true
            settings.setGeolocationEnabled(false)

            // Enable hardware acceleration
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest
                ): Boolean {
                    val url = request.url.toString()
                    // Keep file:// URLs inside WebView
                    if (url.startsWith("file://")) return false
                    // Open external URLs in browser
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Log.e(TAG, "Can't open URL: $url")
                    }
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Log.i(TAG, "Loading: $url")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.i(TAG, "Loaded: $url")
                }

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    val url = request?.url?.toString() ?: "?"
                    val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        error?.description?.toString() ?: "unknown"
                    else "unknown"
                    Log.e(TAG, "WebView error loading $url: $desc")

                    // If main page failed, retry after 1 second
                    if (request?.isForMainFrame == true) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            view?.loadUrl(menuUrl)
                        }, 1000)
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                    Log.d(TAG, "JS: ${message?.message()} [${message?.sourceId()}:${message?.lineNumber()}]")
                    return true
                }
            }

            loadUrl(menuUrl)
        }

        setContentView(webView)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        val currentUrl = webView.url ?: ""
        if (currentUrl != menuUrl && !currentUrl.endsWith("menu.html")) {
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
        // Re-apply fullscreen on resume (Samsung sometimes shows bars)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
