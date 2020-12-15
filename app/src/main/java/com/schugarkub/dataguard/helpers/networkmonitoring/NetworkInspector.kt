package com.schugarkub.dataguard.helpers.networkmonitoring

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetriever
import com.schugarkub.dataguard.helpers.notifications.NotificationSender
import com.schugarkub.dataguard.helpers.notifications.NotificationType
import com.schugarkub.dataguard.model.ApplicationSettings
import com.schugarkub.dataguard.model.NetworkUsageEntity
import com.schugarkub.dataguard.model.NetworkUsageInfo
import com.schugarkub.dataguard.repository.networkusage.NetworkUsageRepository
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

private const val MONITOR_NETWORK_PERIOD_MS = 10_000L

private data class NetworkStatsBundle(
    val uid: Int,
    val networkType: Int,
    val rxBytesRate: Long,
    val txBytesRate: Long
)

class NetworkInspector @Inject constructor(
    private val packageManager: PackageManager,
    private val networkUsageRetriever: NetworkUsageRetriever,
    private val networkUsageRepository: NetworkUsageRepository,
    private val notificationSender: NotificationSender
) {

    private var threshold = ApplicationSettings.DEFAULT_TX_BYTES_THRESHOLD
    private var maxBytesRateDeviation = ApplicationSettings.DEFAULT_MAX_BYTES_RATE_DEVIATION
    private var learningIterations = ApplicationSettings.DEFAULT_LEARNING_ITERATIONS

    fun onThresholdChanged(threshold: Long) {
        synchronized(this) {
            this.threshold = threshold
        }
    }

    fun onMaxBytesRateDeviationChanged(deviation: Float) {
        synchronized(this) {
            maxBytesRateDeviation = deviation
        }
    }

    fun onLearningIterationsChanged(times: Int) {
        synchronized(this) {
            learningIterations = times
        }
    }

    suspend fun monitorNetwork(networkType: Int) {
        val calendar = Calendar.getInstance()
        var lastUsageStats = emptyMap<Int, NetworkUsageInfo>()
        while (true) {
            val endTime = calendar.timeInMillis
            val startTime = endTime - MONITOR_NETWORK_PERIOD_MS

            val usageStats =
                networkUsageRetriever.getNetworkUsageInfo(networkType, startTime, endTime)

            if (lastUsageStats.isNotEmpty()) {
                usageStats.forEach { currentUsage ->
                    val uid = currentUsage.key
                    val current = currentUsage.value
                    val previous = lastUsageStats[uid]
                    if (previous != null) {
                        val rxBytesRate = current.rxBytes - previous.rxBytes
                        val txBytesRate = current.txBytes - previous.txBytes

                        val networkStatsBundle = NetworkStatsBundle(
                            uid, networkType, rxBytesRate, txBytesRate
                        )

                        checkThreshold(networkStatsBundle)
                        checkDeviation(networkStatsBundle)
                    }
                }
            }

            lastUsageStats = usageStats
            delay(MONITOR_NETWORK_PERIOD_MS)
        }
    }

    private fun checkThreshold(bundle: NetworkStatsBundle) {
        if (bundle.txBytesRate > threshold && hasDangerousPermissions(bundle.uid)) {
            packageManager.getPackagesForUid(bundle.uid)?.get(0)?.let { packageName ->
                notificationSender.sendNotification(
                    NotificationType.THRESHOLD_REACHED,
                    bundle.networkType,
                    packageName
                )
            }
        }
    }

    private suspend fun checkDeviation(bundle: NetworkStatsBundle) {
        val packageName = packageManager.getPackagesForUid(bundle.uid)?.get(0) ?: return

        var entity = networkUsageRepository.getEntityByPackageName(packageName)
        if (entity == null) {
            entity = NetworkUsageEntity(
                packageName = packageName,
                averageRxBytesRate = bundle.rxBytesRate,
                averageTxBytesRate = bundle.txBytesRate
            )
            networkUsageRepository.addEntity(entity)
            return
        }

        if (entity.txCalibrationTimes > learningIterations) {
            val deviation = abs(bundle.txBytesRate - entity.averageTxBytesRate).toDouble() /
                    entity.averageTxBytesRate
            if (deviation > maxBytesRateDeviation && hasDangerousPermissions(bundle.uid)) {
                notificationSender.sendNotification(
                    NotificationType.HIGH_DEVIATION, bundle.networkType, packageName
                )
            }
        }

        if (bundle.rxBytesRate > 0 || bundle.txBytesRate > 0) {
            entity.updateAverageRxBytesRate(bundle.rxBytesRate)
            entity.updateAverageTxBytesRate(bundle.txBytesRate)
            networkUsageRepository.updateEntity(entity)
        }
    }

    @Suppress("deprecation")
    private fun hasDangerousPermissions(uid: Int): Boolean {
        val packageName = packageManager.getPackagesForUid(uid)?.get(0) ?: return false

        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        val permissions = packageInfo.requestedPermissions
        val flags = packageInfo.requestedPermissionsFlags

        if (permissions.isEmpty() || flags.isEmpty()) {
            return false
        }

        for (index in permissions.indices) {
            val currentPermission = permissions[index]
            val currentFlag = flags[index]

            if (currentFlag and PackageInfo.REQUESTED_PERMISSION_GRANTED == 0) {
                continue
            }

            val permissionInfo = packageManager.getPermissionInfo(currentPermission, 0)

            val level = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                permissionInfo.protection
            } else {
                permissionInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
            }

            if (level == PermissionInfo.PROTECTION_DANGEROUS) {
                return true
            }
        }

        return false
    }
}