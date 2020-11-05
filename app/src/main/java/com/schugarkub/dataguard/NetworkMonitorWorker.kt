package com.schugarkub.dataguard

import android.app.PendingIntent
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
import com.schugarkub.dataguard.model.NetworkUsageInfo
import com.schugarkub.dataguard.model.NotificationInfo
import com.schugarkub.dataguard.utils.*
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

    private lateinit var networkUsageRetriever: NetworkUsageRetriever

    private lateinit var appPackages: List<AppPackage>

    private val packagesSyncCoroutineScope = CoroutineScope(Dispatchers.Default)
    private val monitorNetworkCoroutineScope = CoroutineScope(Dispatchers.IO)

    @Suppress("deprecation")
    override suspend fun doWork(): Result {
        networkUsageRetriever = NetworkUsageRetriever(applicationContext)

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
        var lastUsageStats = emptyMap<Int, NetworkUsageInfo>()
        while (true) {
            val endTime = calendar.timeInMillis
            val startTime = endTime - MONITOR_NETWORK_PERIOD_MS

            val usageStats =
                networkUsageRetriever.getNetworkUsageInfo(networkType, startTime, endTime)

            if (lastUsageStats.isNotEmpty()) {
                usageStats.forEach { currentUsage ->
                    val lastUsageValue = lastUsageStats[currentUsage.key]
                    lastUsageValue?.let { value ->
                        if (currentUsage.value.txBytes - value.txBytes > TX_BYTES_THRESHOLD) {
                            if (checkUidDangerousPermissions(currentUsage.key).isNotEmpty()) {
                                throwNotificationForUid(currentUsage.key, networkType)
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

    @Suppress("deprecation")
    private fun throwNotificationForUid(uid: Int, networkType: Int) {
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

                val notificationSentIntent = Intent().apply {
                    action = ACTION_NOTIFICATION_SENT
                    putExtra(
                        EXTRA_NOTIFICATION_TITLE,
                        applicationContext.getString(R.string.threshold_reached_notification_title)
                    )
                    putExtra(EXTRA_NOTIFICATION_TIMESTAMP, Calendar.getInstance().timeInMillis)
                    putExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME, appPackage.name)
                    putExtra(
                        EXTRA_NOTIFICATION_NETWORK_TYPE, when (networkType) {
                            ConnectivityManager.TYPE_MOBILE -> NotificationInfo.NetworkType.MOBILE.value
                            ConnectivityManager.TYPE_WIFI -> NotificationInfo.NetworkType.WIFI.value
                            else -> NotificationInfo.NetworkType.UNKNOWN.value
                        }
                    )
                }
                applicationContext.sendBroadcast(notificationSentIntent)
            }
        } catch (e: RemoteException) {
            Timber.e(e, "Couldn't read application info for uid %d", uid)
        }
    }

    private data class AppPackage(
        val name: String,
        val uid: Int
    )
}