package com.schugarkub.dataguard.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.schugarkub.dataguard.view.DataGuardActivity
import com.schugarkub.dataguard.DataGuardApplication
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_WIFI
import com.schugarkub.dataguard.helpers.networkmonitoring.NetworkInspector
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

private const val NETWORK_MONITORING_SERVICE_NOTIFICATION_ID = 100_000

private const val STATE_MONITORING_ENABLED = 1
private const val STATE_MONITORING_DISABLED = -1

class NetworkMonitoringService : Service() {

    private val collectFlowsCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val monitorNetworkCoroutineScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var settingsRepository: ApplicationSettingsRepository

    @Inject
    lateinit var networkInspector: NetworkInspector

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        (application as DataGuardApplication).serviceComponent.inject(this)

        collectFlowsCoroutineScope.launch {
            settingsRepository.getBytesThresholdFlow().collect { value ->
                networkInspector.onThresholdChanged(value)
            }
        }

        collectFlowsCoroutineScope.launch {
            settingsRepository.getMaxBytesRateDeviationFlow().collect { value ->
                networkInspector.onMaxBytesRateDeviationChanged(value)
            }
        }

        collectFlowsCoroutineScope.launch {
            settingsRepository.getLearningIterationsFlow().collect { value ->
                networkInspector.onLearningIterationsChanged(value)
            }
        }

        createNotificationChannels()

        setNetworkMonitoringState(STATE_MONITORING_ENABLED)
        startForeground(NETWORK_MONITORING_SERVICE_NOTIFICATION_ID, buildForegroundNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)

        if (collectFlowsCoroutineScope.isActive) {
            collectFlowsCoroutineScope.cancel()
        }

        setNetworkMonitoringState(STATE_MONITORING_DISABLED)
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

        notificationManager.createNotificationChannel(networkMonitoringChannel)
        notificationManager.createNotificationChannel(suspiciousActivityChannel)
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

    private fun setNetworkMonitoringState(state: Int) {
        when (state) {
            STATE_MONITORING_ENABLED -> {
                Timber.d("Network monitoring enabled")

                if (::networkInspector.isInitialized) {
                    monitorNetworkCoroutineScope.launch {
                        networkInspector.monitorNetwork(NETWORK_TYPE_WIFI)
                    }
                    monitorNetworkCoroutineScope.launch {
                        networkInspector.monitorNetwork(NETWORK_TYPE_MOBILE)
                    }
                }

                isNetworkMonitoringEnabled = true
            }
            STATE_MONITORING_DISABLED -> {
                Timber.d("Network monitoring disabled")

                if (monitorNetworkCoroutineScope.isActive) {
                    monitorNetworkCoroutineScope.cancel()
                }

                isNetworkMonitoringEnabled = false
            }
        }
    }

    companion object {

        var isNetworkMonitoringEnabled: Boolean = false
    }
}