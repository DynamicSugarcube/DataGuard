package com.schugarkub.dataguard.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.schugarkub.dataguard.database.notifications.NotificationsDao
import com.schugarkub.dataguard.database.notifications.NotificationsDatabase
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

const val ACTION_NOTIFICATION_SENT = "com.schugarkub.dataguard.action.NOTIFICATION_SENT"
const val ACTION_NOTIFICATIONS_DATABASE_UPDATED =
    "com.schugarkub.dataguard.action.NOTIFICATIONS_DATABASE_UPDATED"
const val ACTION_NOTIFICATIONS_DATABASE_CLEAN =
    "com.schugarkub.dataguard.action.NOTIFICATIONS_DATABASE_CLEAN"

const val EXTRA_NOTIFICATION_TITLE = "com.schugarkub.dataguard.extra.NOTIFICATION_TITLE"
const val EXTRA_NOTIFICATION_TIMESTAMP = "com.schugarkub.dataguard.extra.NOTIFICATION_TIMESTAMP"
const val EXTRA_NOTIFICATION_APP_PACKAGE_NAME =
    "com.schugarkub.dataguard.extra.NOTIFICATION_APP_PACKAGE_NAME"
const val EXTRA_NOTIFICATION_NETWORK_TYPE =
    "com.schugarkub.dataguard.extra.NOTIFICATION_NETWORK_TYPE"

class NotificationsDatabaseReceiver : BroadcastReceiver() {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)

    private lateinit var notificationsDao: NotificationsDao

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            notificationsDao = NotificationsDatabase.getInstance(context).dao
        }

        if (intent != null && context != null) {
            when (intent.action) {
                ACTION_NOTIFICATION_SENT -> writeToDatabase(context, intent)
                ACTION_NOTIFICATIONS_DATABASE_CLEAN -> cleanDatabase(context)
            }
        }
    }

    private fun writeToDatabase(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE)
        val timestamp =
            intent.getLongExtra(EXTRA_NOTIFICATION_TIMESTAMP, Calendar.getInstance().timeInMillis)
        val packageName = intent.getStringExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME)
        val networkType = intent.getStringExtra(EXTRA_NOTIFICATION_NETWORK_TYPE)
        if (title != null && packageName != null && networkType != null) {
            val notification = NotificationInfo(
                title = title,
                timestamp = timestamp,
                packageName = packageName,
                networkType = when (networkType) {
                    NotificationInfo.NetworkType.MOBILE.value ->
                        NotificationInfo.NetworkType.MOBILE
                    NotificationInfo.NetworkType.WIFI.value ->
                        NotificationInfo.NetworkType.WIFI
                    else -> NotificationInfo.NetworkType.UNKNOWN
                }
            )

            if (::notificationsDao.isInitialized) {
                coroutineScope.launch {
                    notificationsDao.insert(notification)
                    context.sendBroadcast(Intent(ACTION_NOTIFICATIONS_DATABASE_UPDATED))
                }
            }
        }
    }

    private fun cleanDatabase(context: Context) {
        if (::notificationsDao.isInitialized) {
            coroutineScope.launch {
                notificationsDao.clean()
                context.sendBroadcast(Intent(ACTION_NOTIFICATIONS_DATABASE_UPDATED))
            }
        }
    }
}