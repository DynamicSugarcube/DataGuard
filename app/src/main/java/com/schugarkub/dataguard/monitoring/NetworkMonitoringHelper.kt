package com.schugarkub.dataguard.monitoring

import android.content.Context
import androidx.work.*
import java.util.*

object NetworkMonitoringHelper {

    const val ACTION_CONTROL_NETWORK_MONITORING =
        "com.schugarkub.dataguard.action.CONTROL_NETWORK_MONITORING"

    const val EXTRA_NETWORK_MONITORING_ENABLED =
        "com.schugarkub.dataguard.extra.NETWORK_MONITORING_ENABLED"

    const val KEY_NETWORK_MONITORING_ENABLED = "com.schugarkub.dataguard.key.NETWORK_MONITORING_ENABLED"

    private const val NETWORK_MONITOR_WORKER_NAME = "com.schugarkub.dataguard.NetworkMonitor"

    fun scheduleWork(context: Context): UUID {
        val monitorNetworkRequest = OneTimeWorkRequestBuilder<NetworkMonitorWorker>()
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                NETWORK_MONITOR_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                monitorNetworkRequest
            )

        return monitorNetworkRequest.id
    }

    fun cancelWork(context: Context) {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(NETWORK_MONITOR_WORKER_NAME)
    }
}