package com.schugarkub.dataguard.monitoring

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.ConnectivityManager
import android.os.Build
import android.os.RemoteException
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.schugarkub.dataguard.constants.NetworkUsageConstants.MAX_BYTES_RATE_DEVIATION
import com.schugarkub.dataguard.constants.NetworkUsageConstants.MIN_CALIBRATION_TIMES
import com.schugarkub.dataguard.constants.NetworkUsageConstants.TX_BYTES_THRESHOLD
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDao
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDatabase
import com.schugarkub.dataguard.model.*
import com.schugarkub.dataguard.utils.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.math.abs

private const val MONITOR_NETWORK_PERIOD_MS = 10_000L

class NetworkMonitorWorker(context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {

    private lateinit var networkUsageRetriever: NetworkUsageRetriever
    private lateinit var networkUsageDao: NetworkUsageDao

    private val packagesSyncCoroutineScope = CoroutineScope(Dispatchers.Default)
    private val monitorNetworkCoroutineScope = CoroutineScope(Dispatchers.IO)

    @Suppress("deprecation")
    override fun doWork(): Result {
        networkUsageRetriever = NetworkUsageRetriever(applicationContext)
        networkUsageDao = NetworkUsageDatabase.getInstance(applicationContext).dao

        val monitorWifiNetworkDeferred = monitorNetworkCoroutineScope.async {
            monitorNetwork(ConnectivityManager.TYPE_WIFI)
        }

        val monitorMobileNetworkDeferred = monitorNetworkCoroutineScope.async {
            monitorNetwork(ConnectivityManager.TYPE_MOBILE)
        }

        runBlocking {
            monitorWifiNetworkDeferred.await()
            monitorMobileNetworkDeferred.await()
        }

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        packagesSyncCoroutineScope.cancel()
        monitorNetworkCoroutineScope.cancel()
    }

    private suspend fun monitorNetwork(networkType: Int) {
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

                        inspectRelatedEntities(networkType, uid, rxBytesRate, txBytesRate)

                        if (txBytesRate > TX_BYTES_THRESHOLD) {
                            if (checkUidDangerousPermissions(uid).isNotEmpty()) {
                                val packageName =
                                    applicationContext.packageManager.getPackagesForUid(uid)?.get(0)
                                packageName?.let {
                                    NotificationsHelper.sendNotification(
                                        applicationContext,
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
        applicationContext.packageManager.getPackagesForUid(uid)?.forEach { packageName ->
            val entity = networkUsageDao.getByPackageName(packageName)
            if (entity != null) {
                entity.also {
                    if (entity.txCalibrationTimes > MIN_CALIBRATION_TIMES) {
                        val txDeviation =
                            abs(txBytesRate - entity.averageTxBytesRate).toDouble() / entity.averageTxBytesRate
                        if (txDeviation > MAX_BYTES_RATE_DEVIATION) {
                            NotificationsHelper.sendNotification(
                                applicationContext,
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
                        networkUsageDao.update(it)
                    }
                }
            } else {
                NetworkUsageEntity(
                    packageName = packageName,
                    averageRxBytesRate = rxBytesRate,
                    averageTxBytesRate = txBytesRate
                ).also {
                    networkUsageDao.insert(it)
                }
            }
        }
    }

    @Suppress("deprecation")
    private fun checkUidDangerousPermissions(uid: Int): List<String> {
        val dangerousGrantedPermissions = mutableListOf<String>()

        try {
            val pm = applicationContext.packageManager
            val packages = pm.getPackagesForUid(uid)
            packages?.forEach { packageName ->
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                val permissions = packageInfo.requestedPermissions
                val permissionsFlags = packageInfo.requestedPermissionsFlags

                if (permissions.isNullOrEmpty() or permissionsFlags.isEmpty()) {
                    return emptyList()
                }

                for (i in permissions.indices) {
                    if (permissionsFlags[i].and(PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                        val permissionInfo = pm.getPermissionInfo(permissions[i], 0)
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