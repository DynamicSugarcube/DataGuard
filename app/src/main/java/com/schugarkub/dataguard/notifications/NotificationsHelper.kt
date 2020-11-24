package com.schugarkub.dataguard.notifications

import android.app.Notification
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

object NotificationsHelper {

    enum class SuspiciousActivityType(val descriptionResId: Int) {
        THRESHOLD_REACHED(R.string.threshold_reached_notification_text),
        HIGH_DEVIATION(R.string.high_deviation_notification_text)
    }

    fun sendNotification(
        context: Context,
        suspiciousActivityType: SuspiciousActivityType,
        networkType: Int,
        packageName: String
    ) {
        val info = context.packageManager.getApplicationInfo(packageName, 0)

        val notification = buildNotification(context, suspiciousActivityType, info)

        sendNotification(context, notification, info.uid)

        sendNotificationSentBroadcast(context, suspiciousActivityType, networkType, packageName)
    }

    private fun buildNotification(
        context: Context,
        type: SuspiciousActivityType,
        info: ApplicationInfo
    ): Notification {
        val channelId =
            context.getString(R.string.suspicious_activity_detected_notification_channel_id)
        val title = context.getString(R.string.suspicious_activity_detected_notification_title)

        val appPackageName = info.packageName
        val appLabel = info.loadLabel(context.packageManager)
        val appIcon = info.loadIcon(context.packageManager).toBitmap()

        val text = context.getString(type.descriptionResId, appLabel)

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
            .setSmallIcon(R.drawable.ic_security)
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
        suspiciousActivityType: SuspiciousActivityType,
        networkType: Int,
        packageName: String
    ) {
        val intent = Intent(ACTION_NOTIFICATION_SENT).apply {
            putExtra(EXTRA_NOTIFICATION_DESCRIPTION, suspiciousActivityType.descriptionResId)
            putExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME, packageName)
            putExtra(EXTRA_NOTIFICATION_NETWORK_TYPE, networkType)
        }

        context.sendBroadcast(intent)
    }
}