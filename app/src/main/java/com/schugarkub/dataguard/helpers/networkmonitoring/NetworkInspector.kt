package com.schugarkub.dataguard.helpers.networkmonitoring

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.RemoteException
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDatabase
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetriever
import com.schugarkub.dataguard.model.ApplicationSettings
import com.schugarkub.dataguard.model.NetworkUsageEntity
import com.schugarkub.dataguard.model.NetworkUsageInfo
import com.schugarkub.dataguard.notifications.NotificationsHelper
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

private const val MONITOR_NETWORK_PERIOD_MS = 10_000L

class NetworkInspector @Inject constructor(
    application: Application, // TODO Remove when networkUsageDatabaseDao injected
    private val packageManager: PackageManager,
    private val networkUsageRetriever: NetworkUsageRetriever
) {

    private val context = application.applicationContext

    // TODO Remove when networkUsageDatabaseDao injected
    private val networkUsageDatabaseDao = NetworkUsageDatabase.getInstance(context).dao

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

            val usageStats = networkUsageRetriever.getNetworkUsageInfo(networkType, startTime, endTime)

            if (lastUsageStats.isNotEmpty()) {
                usageStats.forEach { currentUsage ->
                    val uid = currentUsage.key
                    val current = currentUsage.value
                    val previous = lastUsageStats[uid]
                    if (previous != null) {
                        val rxBytesRate = current.rxBytes - previous.rxBytes
                        val txBytesRate = current.txBytes - previous.txBytes

                        inspectRelatedEntities(networkType, uid, rxBytesRate, txBytesRate)

                        if (txBytesRate > threshold) {
                            if (checkUidDangerousPermissions(uid).isNotEmpty()) {
                                val packageName = packageManager.getPackagesForUid(uid)?.get(0)
                                packageName?.let {
                                    NotificationsHelper.sendNotification(
                                        context,
                                        NotificationsHelper.SuspiciousActivityType.THRESHOLD_REACHED,
                                        networkType,
                                        packageName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            lastUsageStats = usageStats
            delay(MONITOR_NETWORK_PERIOD_MS)
        }
    }

    private suspend fun inspectRelatedEntities(
        networkType: Int,
        uid: Int,
        rxBytesRate: Long,
        txBytesRate: Long
    ) {
        packageManager.getPackagesForUid(uid)?.forEach { packageName ->
            val entity = networkUsageDatabaseDao.getByPackageName(packageName)
            if (entity != null) {
                entity.also {
                    if (entity.txCalibrationTimes > learningIterations) {
                        val txDeviation =
                            abs(txBytesRate - entity.averageTxBytesRate).toDouble() / entity.averageTxBytesRate
                        if (txDeviation > maxBytesRateDeviation) {
                            NotificationsHelper.sendNotification(
                                context,
                                NotificationsHelper.SuspiciousActivityType.HIGH_DEVIATION,
                                networkType,
                                packageName
                            )
                            return
                        }
                    }

                    if (rxBytesRate > 0 || txBytesRate > 0) {
                        it.updateAverageRxBytesRate(rxBytesRate)
                        it.updateAverageTxBytesRate(txBytesRate)
                        networkUsageDatabaseDao.update(it)
                    }
                }
            } else {
                NetworkUsageEntity(
                    packageName = packageName,
                    averageRxBytesRate = rxBytesRate,
                    averageTxBytesRate = txBytesRate
                ).also {
                    networkUsageDatabaseDao.insert(it)
                }
            }
        }
    }

    @Suppress("deprecation")
    private fun checkUidDangerousPermissions(uid: Int): List<String> {
        val dangerousGrantedPermissions = mutableListOf<String>()

        try {
            val packages = packageManager.getPackagesForUid(uid)
            packages?.forEach { packageName ->
                val packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                val permissions = packageInfo.requestedPermissions ?: emptyArray<String>()
                val permissionsFlags = packageInfo.requestedPermissionsFlags ?: intArrayOf()

                if (permissions.isNullOrEmpty() or permissionsFlags.isEmpty()) {
                    return emptyList()
                }

                for (i in permissions.indices) {
                    if (permissionsFlags[i].and(PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                        val permissionInfo = packageManager.getPermissionInfo(permissions[i], 0)
                        val level = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            permissionInfo.protection
                        } else {
                            permissionInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
                        }
                        if (level == PermissionInfo.PROTECTION_DANGEROUS) {
                            dangerousGrantedPermissions.add(permissions[i])
                        }
                    }
                }
            }
        } catch (e: RemoteException) {
            Timber.e(e, "Couldn't read permissions for uid %d", uid)
            return emptyList()
        }

        return dangerousGrantedPermissions
    }
}