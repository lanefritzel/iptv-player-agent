package com.frequentsee.tv.agent

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface
import androidx.core.app.NotificationCompat
import com.frequentsee.tv.agent.server.CastServer
import java.io.IOException

class ReceiverService : Service() {

    private var castServer: CastServer? = null

    companion object {
        private const val TAG = "ReceiverService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "cast_receiver_channel"
        private const val SERVER_PORT = 8080
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start foreground service with notification
        val notification = createNotification("Starting cast receiver...")
        startForeground(NOTIFICATION_ID, notification)

        // Start HTTP server
        startServer()

        return START_STICKY
    }

    private fun startServer() {
        try {
            castServer = CastServer(applicationContext, SERVER_PORT).apply {
                start()
            }

            val ipAddress = getIpAddress()
            val message = if (ipAddress != null) {
                "Cast receiver running on http://$ipAddress:$SERVER_PORT"
            } else {
                "Cast receiver running on port $SERVER_PORT"
            }

            Log.d(TAG, message)

            // Update notification with IP address
            val notification = createNotification(message)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server", e)
            stopSelf()
        }
    }

    private fun getIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.asSequence()
                ?.filter { it.isUp && !it.isLoopback }
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.filterIsInstance<Inet4Address>()
                ?.map { it.hostAddress }
                ?.firstOrNull { it != "127.0.0.1" }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get IP address", e)
            null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cast Receiver",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when the cast receiver is running"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FrequentSee TV Agent")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload) // Using system icon for now
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        castServer?.stop()
        castServer = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
