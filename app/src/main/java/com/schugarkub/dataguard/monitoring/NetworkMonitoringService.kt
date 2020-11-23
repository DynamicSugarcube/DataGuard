package com.schugarkub.dataguard.monitoring

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_WIFI
import com.schugarkub.dataguard.utils.NotificationsHelper
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

        NotificationsHelper.createNotificationChannel(
            applicationContext,
            NotificationsHelper.NotificationType.NETWORK_MONITORING
        )

        val notification =
            NotificationsHelper.buildForegroundServiceNotification(applicationContext)
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
}