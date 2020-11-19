package com.schugarkub.dataguard.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_WIFI
import kotlinx.coroutines.*
import timber.log.Timber

// TODO Move to res
private const val NOTIFICATION_CHANNEL_ID = "com.schugarkub.dataguard.NetworkMonitoringService"
private const val NOTIFICATION_CHANNEL_NAME = "NetworkMonitoringServiceChannel"

private const val NETWORK_MONITORING_SERVICE_NOTIFICATION_ID = 100_000

class NetworkMonitoringService : Service() {

    private val binder = NetworkMonitoringBinder()

    private val monitorNetworkCoroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var networkInspector: NetworkInspector

    private var isMonitoringEnabled = false

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        networkInspector = NetworkInspector(applicationContext)
        createServiceNotificationChannel()
        val notification = buildServiceNotification()
        startForeground(NETWORK_MONITORING_SERVICE_NOTIFICATION_ID, notification)
        startNetworkMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopNetworkMonitoring()
    }

    private fun startNetworkMonitoring() {
        Timber.d("Start network monitoring")
        if (::networkInspector.isInitialized) {
            monitorNetworkCoroutineScope.launch {
                networkInspector.monitorNetwork(NETWORK_TYPE_WIFI)
            }
            monitorNetworkCoroutineScope.launch {
                networkInspector.monitorNetwork(NETWORK_TYPE_MOBILE)
            }
        }
        isMonitoringEnabled = true
    }

    private fun stopNetworkMonitoring() {
        Timber.d("Stop network monitoring")
        if (monitorNetworkCoroutineScope.isActive) {
            monitorNetworkCoroutineScope.cancel()
        }
        isMonitoringEnabled = false
    }

    inner class NetworkMonitoringBinder : Binder() {

        val isNetworkMonitoringEnabled: Boolean
            get() = this@NetworkMonitoringService.isMonitoringEnabled

        fun startNetworkMonitoring() {
            this@NetworkMonitoringService.startNetworkMonitoring()
        }

        fun stopNetworkMonitoring() {
            this@NetworkMonitoringService.stopNetworkMonitoring()
        }
    }

    // TODO Move to notification helper
    private fun createServiceNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    // TODO Move to notification helper
    private fun buildServiceNotification(): Notification {
        // TODO Refactor
        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("NetworkMonitoringService")
            .setContentText("NetworkMonitoringService")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
    }
}