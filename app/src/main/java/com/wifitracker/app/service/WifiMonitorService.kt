package com.wifitracker.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wifitracker.app.MainActivity
import com.wifitracker.app.WifiTrackerApp
import com.wifitracker.app.data.WifiSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * এই সার্ভিসটি সবসময় ব্যাকগ্রাউন্ডে চলে এবং ডিভাইসটি কখন WiFi এর সাথে সংযুক্ত/বিচ্ছিন্ন হচ্ছে
 * তা ট্র্যাক করে প্রতিটি সেশন ডাটাবেজে সংরক্ষণ করে।
 */
class WifiMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "wifi_monitor_channel"
        const val NOTIFICATION_ID = 1001
        private const val HEARTBEAT_MS = 30_000L
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var currentSessionId: Long? = null
    private var heartbeatJob: Job? = null

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onWifiConnected()
            }

            override fun onLost(network: Network) {
                onWifiDisconnected()
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // অ্যাপ চালু হওয়ার সময় ইতিমধ্যে WiFi এর সাথে সংযুক্ত থাকলে সেটিও ধরার চেষ্টা
        if (isCurrentlyOnWifi()) {
            onWifiConnected()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun isCurrentlyOnWifi(): Boolean {
        val active = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(active) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun onWifiConnected() {
        if (currentSessionId != null) return
        val db = (application as WifiTrackerApp).database
        scope.launch {
            val now = System.currentTimeMillis()
            val id = db.wifiSessionDao().insert(WifiSession(startTime = now, endTime = now))
            currentSessionId = id
            startHeartbeat()
        }
    }

    private fun onWifiDisconnected() {
        val id = currentSessionId ?: return
        currentSessionId = null
        heartbeatJob?.cancel()
        val db = (application as WifiTrackerApp).database
        scope.launch {
            val session = db.wifiSessionDao().getById(id) ?: return@launch
            db.wifiSessionDao().update(session.copy(endTime = System.currentTimeMillis()))
        }
    }

    /** সার্ভিস হঠাৎ বন্ধ হয়ে গেলেও ডেটা যেন বেশি না হারায়, তাই চলমান সেশনের endTime নিয়মিত আপডেট হয়। */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                delay(HEARTBEAT_MS)
                val id = currentSessionId ?: break
                val db = (application as WifiTrackerApp).database
                val session = db.wifiSessionDao().getById(id) ?: break
                db.wifiSessionDao().update(session.copy(endTime = System.currentTimeMillis()))
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WiFi মনিটরিং",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "WiFi সংযোগের সময় ট্র্যাক করার জন্য ব্যাকগ্রাউন্ড সার্ভিস"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WiFi সময় ট্র্যাক হচ্ছে")
            .setContentText("আপনার WiFi সংযোগের সময় হিসাব করা হচ্ছে")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        onWifiDisconnected()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
