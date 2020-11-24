package com.schugarkub.dataguard.monitoring

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.RemoteException
import com.schugarkub.dataguard.constants.NetworkUsageConstants
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDatabase
import com.schugarkub.dataguard.model.NetworkUsageEntity
import com.schugarkub.dataguard.model.NetworkUsageInfo
import com.schugarkub.dataguard.utils.NetworkUsageRetriever
import com.schugarkub.dataguard.utils.NotificationsHelper
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*
import kotlin.math.abs

private const val MONITOR_NETWORK_PERIOD_MS = 10_000L

class NetworkInspector(private val context: Context) {

    private val packageManager = context.packageManager

    private val networkUsageRetriever = NetworkUsageRetriever(context)

    private val networkUsageDatabaseDao = NetworkUsageDatabase.getInstance(context).dao

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

                        if (txBytesRate > NetworkUsageConstants.TX_BYTES_THRESHOLD) {
                            if (checkUidDangerousPermissions(uid).isNotEmpty()) {
                                val packageName = packageManager.getPackagesForUid(uid)?.get(0)
                                packageName?.let {
                                    NotificationsHelper.sendNotification(
                                        context,
                                        NotificationsHelper.NotificationType.THRESHOLD_REACHED,
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
                    if (entity.txCalibrationTimes > NetworkUsageConstants.MIN_CALIBRATION_TIMES) {
                        val txDeviation =
                            abs(txBytesRate - entity.averageTxBytesRate).toDouble() / entity.averageTxBytesRate
                        if (txDeviation > NetworkUsageConstants.MAX_BYTES_RATE_DEVIATION) {
                            NotificationsHelper.sendNotification(
                                context,
                                NotificationsHelper.NotificationType.HIGH_DEVIATION,
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
                val permissions = packageInfo.requestedPermissions
                val permissionsFlags = packageInfo.requestedPermissionsFlags

                if (permissions.isNullOrEmpty() or permissionsFlags.isEmpty()) {
                    return emptyList()
                }

                for (i in permissions.indices) {
                    if (permissionsFlags[i].and(PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                        val permissionInfo = packageManager.getPermissionInfo(permissions[i], 0)
                        val level = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            permissionInfo.protection
                        } else {
                            permissionInfo.protectionLevel
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