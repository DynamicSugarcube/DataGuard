package com.schugarkub.dataguard

import android.app.PendingIntent
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.RemoteException
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

private const val PACKAGES_SYNC_PERIOD_MS = 60_000L
private const val MONITOR_NETWORK_PERIOD_MS = 10_000L

// TODO Replace with correct threshold
private const val TX_BYTES_THRESHOLD = 10_000L

class NetworkMonitorWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private lateinit var networkStatsManager: NetworkStatsManager

    private lateinit var appPackages: List<AppPackage>

    private val packagesSyncCoroutineScope = CoroutineScope(Dispatchers.Default)
    private val monitorNetworkCoroutineScope = CoroutineScope(Dispatchers.IO)

    @Suppress("deprecation")
    override suspend fun doWork(): Result {
        networkStatsManager = applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE)
                as NetworkStatsManager

        packagesSyncCoroutineScope.launch {
            syncAppPackages()
        }

        monitorNetworkCoroutineScope.launch {
            monitorNetwork(ConnectivityManager.TYPE_WIFI)
        }

        monitorNetworkCoroutineScope.launch {
            monitorNetwork(ConnectivityManager.TYPE_MOBILE)
        }

        return Result.success()
    }

    private suspend fun syncAppPackages() {
        val packages = getAppPackages()
        synchronized(this) {
            appPackages = packages
        }
        delay(PACKAGES_SYNC_PERIOD_MS)
    }

    private fun getAppPackages(): List<AppPackage> {
        val pm = applicationContext.packageManager
        return pm.getInstalledApplications(0).map {
            AppPackage(
                name = it.packageName,
                uid = it.uid
            )
        }
    }

    private suspend fun monitorNetwork(networkType: Int) {
        val calendar = Calendar.getInstance()
        var lastUsageStats = mutableMapOf<Int, NetworkUsage>()
        while (true) {
            val endTime = calendar.timeInMillis
            val startTime = endTime - MONITOR_NETWORK_PERIOD_MS
            val networkStats =
                networkStatsManager.querySummary(networkType, null, startTime, endTime)

            val usageStats = mutableMapOf<Int, NetworkUsage>()

            val bucket = NetworkStats.Bucket()
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket)
                val uid = bucket.uid
                val rxBytes = bucket.rxBytes
                val txBytes = bucket.txBytes
                if (usageStats.containsKey(uid)) {
                    usageStats[uid]?.let {
                        it.rxBytes += rxBytes
                        it.txBytes += txBytes
                    }
                } else {
                    usageStats[uid] = NetworkUsage(rxBytes, txBytes)
                }
            }

            networkStats.close()

            if (lastUsageStats.isNotEmpty()) {
                usageStats.forEach { currentUsage ->
                    val lastUsageValue = lastUsageStats[currentUsage.key]
                    lastUsageValue?.let { value ->
                        if (currentUsage.value.txBytes - value.txBytes > TX_BYTES_THRESHOLD) {
                            if (checkUidDangerousPermissions(currentUsage.key).isNotEmpty()) {
                                throwNotificationForUid(currentUsage.key)
                            }
                        }
                    }
                }
            }

            lastUsageStats = usageStats
            delay(MONITOR_NETWORK_PERIOD_MS)
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

    private fun throwNotificationForUid(uid: Int) {
        try {
            val pm = applicationContext.packageManager
            val appPackage = appPackages.find { it.uid == uid }
            appPackage?.let {
                val appInfo = pm.getApplicationInfo(appPackage.name, 0)

                val icon = appInfo.loadIcon(pm).toBitmap()
                val label = appInfo.loadLabel(pm)

                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${appInfo.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                val notificationBuilder =
                    NotificationCompat.Builder(
                        applicationContext,
                        applicationContext.getString(R.string.threshold_reached_notification_channel_id)
                    )
                        .setSmallIcon(R.drawable.ic_warning)
                        .setLargeIcon(icon)
                        .setContentTitle(applicationContext.getString(R.string.threshold_reached_notification_title))
                        .setContentText("$label: Suspicious activity detected!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(uid, notificationBuilder.build())
                }
            }
        } catch (e: RemoteException) {
            Timber.e(e, "Couldn't read application info for uid %d", uid)
        }
    }

    private data class AppPackage(
        val name: String,
        val uid: Int
    )

    private data class NetworkUsage(
        var rxBytes: Long,
        var txBytes: Long
    )
}