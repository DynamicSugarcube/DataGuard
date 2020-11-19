package com.schugarkub.dataguard.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.model.NotificationInfo
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.utils.ConnectivityWrapper.NETWORK_TYPE_WIFI
import java.util.*

object NotificationsHelper {

    enum class NotificationType {
        THRESHOLD_REACHED,
        HIGH_DEVIATION
    }

    fun createNotificationChannels(context: Context) {
        createNotificationChannel(context, NotificationType.THRESHOLD_REACHED)
        createNotificationChannel(context, NotificationType.HIGH_DEVIATION)
    }

    fun sendNotification(
        context: Context,
        notificationType: NotificationType,
        networkType: Int,
        packageName: String
    ) {
        val info = context.packageManager.getApplicationInfo(packageName, 0)

        val notification = buildNotification(context, notificationType, info)

        sendNotification(context, notification, info.uid)

        sendNotificationSentBroadcast(context, notificationType, networkType, packageName)
    }

    private fun createNotificationChannel(context: Context, notificationType: NotificationType) {
        val id = getNotificationChannelIdByType(context, notificationType)
        val name = getNotificationChannelNameByType(context, notificationType)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        context: Context,
        type: NotificationType,
        info: ApplicationInfo
    ): Notification {
        val channelId = getNotificationChannelIdByType(context, type)
        val title = getNotificationTitleByType(context, type)

        val appPackageName = info.packageName
        val appLabel = info.loadLabel(context.packageManager)
        val appIcon = info.loadIcon(context.packageManager).toBitmap()

        val text = "$appLabel: Suspicious activity detected!"

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:$appPackageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            0
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_warning)
            .setLargeIcon(appIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun sendNotification(
        context: Context,
        notification: Notification,
        notificationId: Int
    ) {
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }
    }

    private fun sendNotificationSentBroadcast(
        context: Context,
        notificationType: NotificationType,
        networkType: Int,
        packageName: String
    ) {
        val title = getNotificationTitleByType(context, notificationType)
        val timestamp = Calendar.getInstance().timeInMillis
        val networkTypeString = when (networkType) {
            NETWORK_TYPE_MOBILE -> NotificationInfo.NetworkType.MOBILE.value
            NETWORK_TYPE_WIFI -> NotificationInfo.NetworkType.WIFI.value
            else -> NotificationInfo.NetworkType.UNKNOWN.value
        }

        val intent = Intent().apply {
            action = ACTION_NOTIFICATION_SENT
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_TIMESTAMP, timestamp)
            putExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME, packageName)
            putExtra(EXTRA_NOTIFICATION_NETWORK_TYPE, networkTypeString)
        }

        context.sendBroadcast(intent)
    }

    private fun getNotificationChannelIdByType(context: Context, type: NotificationType): String {
        return when (type) {
            NotificationType.THRESHOLD_REACHED ->
                context.getString(R.string.threshold_reached_notification_channel_id)
            NotificationType.HIGH_DEVIATION ->
                context.getString(R.string.high_deviation_notification_channel_id)
        }
    }

    private fun getNotificationChannelNameByType(context: Context, type: NotificationType): String {
        return when (type) {
            NotificationType.THRESHOLD_REACHED ->
                context.getString(R.string.threshold_reached_notification_channel_name)
            NotificationType.HIGH_DEVIATION ->
                context.getString(R.string.high_deviation_notification_channel_name)
        }
    }

    private fun getNotificationTitleByType(context: Context, type: NotificationType): String {
        return when (type) {
            NotificationType.THRESHOLD_REACHED ->
                context.getString(R.string.threshold_reached_notification_title)
            NotificationType.HIGH_DEVIATION ->
                context.getString(R.string.high_deviation_notification_title)
        }
    }
}