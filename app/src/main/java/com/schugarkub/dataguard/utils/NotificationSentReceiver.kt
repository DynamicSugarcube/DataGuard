package com.schugarkub.dataguard.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.schugarkub.dataguard.database.NotificationsDao
import com.schugarkub.dataguard.database.NotificationsDatabase
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

const val ACTION_NOTIFICATION_SENT = "com.schugarkub.dataguard.action.NOTIFICATION_SENT"
const val ACTION_NOTIFICATION_DATABASE_UPDATED =
    "com.schugarkub.dataguard.action.NOTIFICATION_DATABASE_UPDATED"

const val EXTRA_NOTIFICATION_TITLE = "com.schugarkub.dataguard.extra.NOTIFICATION_TITLE"
const val EXTRA_NOTIFICATION_TIMESTAMP = "com.schugarkub.dataguard.extra.NOTIFICATION_TIMESTAMP"
const val EXTRA_NOTIFICATION_APP_PACKAGE_NAME =
    "com.schugarkub.dataguard.extra.NOTIFICATION_APP_PACKAGE_NAME"
const val EXTRA_NOTIFICATION_NETWORK_TYPE =
    "com.schugarkub.dataguard.extra.NOTIFICATION_NETWORK_TYPE"

class NotificationSentReceiver : BroadcastReceiver() {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)

    private lateinit var notificationsDao: NotificationsDao

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            notificationsDao = NotificationsDatabase.getInstance(context).dao
        }

        intent?.let {
            val title = it.getStringExtra(EXTRA_NOTIFICATION_TITLE)
            val timestamp =
                it.getLongExtra(EXTRA_NOTIFICATION_TIMESTAMP, Calendar.getInstance().timeInMillis)
            val packageName = it.getStringExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME)
            val networkType = it.getStringExtra(EXTRA_NOTIFICATION_NETWORK_TYPE)
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
                    context?.let {
                        writeToDatabase(context, notification)
                    }
                }
            }
        }
    }

    private fun writeToDatabase(context: Context, notification: NotificationInfo) {
        coroutineScope.launch {
            notificationsDao.insert(notification)
            context.sendBroadcast(Intent(ACTION_NOTIFICATION_DATABASE_UPDATED))
        }
    }
}