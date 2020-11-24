package com.schugarkub.dataguard.monitoring

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.schugarkub.dataguard.DataGuardActivity
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_WIFI
import kotlinx.coroutines.*
import timber.log.Timber

const val NETWORK_MONITORING_SERVICE_NOTIFICATION_ID = 100_000

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

        createNotificationChannels()

        startNetworkMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopNetworkMonitoring()
    }

    private fun createNotificationChannels() {
        val networkMonitoringChannel = NotificationChannel(
            applicationContext.getString(R.string.network_monitoring_notification_channel_id),
            applicationContext.getString(R.string.network_monitoring_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )

        val suspiciousActivityChannel = NotificationChannel(
            applicationContext.getString(R.string.suspicious_activity_detected_notification_channel_id),
            applicationContext.getString(R.string.suspicious_activity_detected_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )

        with(applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            createNotificationChannel(networkMonitoringChannel)
            createNotificationChannel(suspiciousActivityChannel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val channelId =
            applicationContext.getString(R.string.network_monitoring_notification_channel_id)
        val title = applicationContext.getString(R.string.network_monitoring_notification_title)
        val pendingIntent = Intent(applicationContext, DataGuardActivity::class.java).let {
            PendingIntent.getActivity(applicationContext, 0, it, 0)
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_security)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
    }

    private fun startNetworkMonitoring() {
        Timber.d("Start network monitoring")
        startForeground(NETWORK_MONITORING_SERVICE_NOTIFICATION_ID, buildForegroundNotification())

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
        stopForeground(true)

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
}