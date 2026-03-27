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
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val menuUrl = "file:///android_asset/games/menu.html"
    private val TAG = "PixelClassics"

    private val debugLog = StringBuilder()

    private fun dlog(msg: String) {
        Log.i(TAG, msg)
        debugLog.append("[${System.currentTimeMillis()}] $msg\n")
    }

    private fun sendDebugEmail() {
        try {
            val deviceInfo = """
                Device: ${Build.MANUFACTURER} ${Build.MODEL}
                Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                CPU: ${Build.SUPPORTED_ABIS.joinToString(", ")}
                Board: ${Build.BOARD}
                Hardware: ${Build.HARDWARE}
                SOC: ${if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER + " " + Build.SOC_MODEL else "N/A"}
            """.trimIndent()

            val body = "=== DEVICE INFO ===\n$deviceInfo\n\n=== DEBUG LOG ===\n$debugLog"

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("elyssov@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Pixel Classics Debug: ${Build.MANUFACTURER} ${Build.MODEL}")
                putExtra(Intent.EXTRA_TEXT, body)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback — try ACTION_SEND
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("elyssov@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Pixel Classics Debug: ${Build.MANUFACTURER} ${Build.MODEL}")
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                startActivity(Intent.createChooser(fallback, "Send debug log"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can't send debug email: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dlog("onCreate START")

        // Global crash handler — send email on crash
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            dlog("CRASH: ${sw}")
            sendDebugEmail()
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Fullscreen
        dlog("Setting fullscreen")
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

        dlog("Fullscreen OK")

        // Start Score Service in background — delay 5s, catch everything
        Handler(Looper.getMainLooper()).postDelayed({
            dlog("Starting ScoreService (delayed)")
            try {
                val intent = Intent(this, ScoreService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                dlog("ScoreService started OK")
            } catch (e: Exception) {
                dlog("ScoreService FAILED: ${e.message}")
            } catch (e: Error) {
                dlog("ScoreService ERROR: ${e.message}")
            }
        }, 5000)

        // WebView setup
        dlog("Creating WebView")
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
                    dlog("WebView loading: $url")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    dlog("WebView loaded: $url")
                }

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    val url = request?.url?.toString() ?: "?"
                    val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        error?.description?.toString() ?: "unknown"
                    else "unknown"
                    dlog("WebView ERROR loading $url: $desc")

                    // If main page failed, retry after 1 second
                    if (request?.isForMainFrame == true) {
                        dlog("Main frame failed — retrying in 1s")
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
        dlog("WebView set as content view")

        // Auto-send debug log after 8 seconds if still on black screen
        // (user can also shake to send — but simpler: just auto-open email)
        Handler(Looper.getMainLooper()).postDelayed({
            dlog("8s timeout — offering debug email")
            // Only send if we haven't loaded successfully yet
            if (debugLog.contains("WebView loaded")) {
                dlog("Page loaded OK — no debug needed")
            } else {
                dlog("Page NOT loaded after 8s — sending debug email")
                sendDebugEmail()
            }
        }, 8000)
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
