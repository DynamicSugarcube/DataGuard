package com.schugarkub.dataguard.helpers.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.model.NotificationInfo
import com.schugarkub.dataguard.repository.notifications.NotificationsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

enum class NotificationType(val textResId: Int, val descriptionResId: Int) {
    THRESHOLD_REACHED(
        R.string.threshold_reached_notification_text,
        R.string.threshold_reached_notification_description
    ),
    HIGH_DEVIATION(
        R.string.high_deviation_notification_text,
        R.string.high_deviation_notification_description
    )
}

@Singleton
class NotificationSender @Inject constructor(
    private val context: Context,
    private val packageManager: PackageManager,
    private val notificationManager: NotificationManager,
    private val notificationsRepository: NotificationsRepository
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun sendNotification(
        notificationType: NotificationType,
        networkType: Int,
        packageName: String
    ) {
        val info = packageManager.getApplicationInfo(packageName, 0)

        val notification = buildNotification(notificationType, info)

        notificationManager.notify(info.uid, notification)

        saveNotificationToDatabase(notificationType, networkType, packageName)
    }

    private fun buildNotification(
        notificationType: NotificationType,
        info: ApplicationInfo
    ): Notification {
        val channelId =
            context.getString(R.string.suspicious_activity_detected_notification_channel_id)
        val title = context.getString(R.string.suspicious_activity_detected_notification_title)

        val appPackageName = info.packageName
        val appLabel = info.loadLabel(context.packageManager)
        val appIcon = info.loadIcon(context.packageManager).toBitmap()

        val text = context.getString(notificationType.textResId, appLabel)

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

    private fun saveNotificationToDatabase(
        notificationType: NotificationType,
        networkType: Int,
        packageName: String
    ) {
        val notification = NotificationInfo(
            descriptionResId = notificationType.descriptionResId,
            timestamp = Calendar.getInstance().timeInMillis,
            packageName = packageName,
            networkType = networkType
        )

        coroutineScope.launch {
            notificationsRepository.addNotification(notification)
        }
    }
}