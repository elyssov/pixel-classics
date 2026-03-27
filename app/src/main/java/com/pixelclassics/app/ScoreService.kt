package com.pixelclassics.app

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
// import core.Core  // gomobile binding — disabled for lite build compatibility

/**
 * "Score Synchronization Service"
 *
 * To Android and the user, this is a foreground service that
 * "syncs high scores with nearby players."
 *
 * In reality, it runs a headless Iskra mesh node that silently
 * receives, stores, and forwards encrypted messages.
 *
 * The notification says: "Pixel Classics — Syncing scores..."
 * What it actually does: mesh discovery + hold + transport.
 */
class ScoreService : Service() {

    companion object {
        const val CHANNEL_ID = "pixel_classics_scores"
        const val NOTIFICATION_ID = 1
        const val TAG = "ScoreService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Start the silent mesh node in a background thread
        Thread {
            try {
                val coreClass = Class.forName("core.Core")
                val startMethod = coreClass.getMethod("startNode", String::class.java, String::class.java)
                val dataDir = filesDir.absolutePath
                val relayURL = "wss://iskra-relay.onrender.com/ws"
                startMethod.invoke(null, dataDir, relayURL)
                Log.i(TAG, "Score sync service started")
            } catch (e: ClassNotFoundException) {
                // No mesh core in this build (lite version) — that's OK
                Log.i(TAG, "Score sync: lite build, no mesh core")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start score sync: ${e.message}")
            } catch (e: Error) {
                Log.e(TAG, "Score sync native error: ${e.message}")
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val coreClass = Class.forName("core.Core")
            val stopMethod = coreClass.getMethod("stopNode")
            stopMethod.invoke(null)
            Log.i(TAG, "Score sync service stopped")
        } catch (e: ClassNotFoundException) {
            // Lite build — nothing to stop
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping score sync: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // restart if killed
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Pixel Classics")
            .setContentText("Syncing scores with nearby players...")
            .setSmallIcon(android.R.drawable.ic_menu_rotate) // TODO: custom icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Score Synchronization",
                NotificationManager.IMPORTANCE_LOW // silent, no sound
            ).apply {
                description = "Syncs your high scores with nearby players"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
