package com.schugarkub.dataguard.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_UNKNOWN
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

const val EXTRA_NOTIFICATION_DESCRIPTION = "com.schugarkub.dataguard.extra.NOTIFICATION_DESCRIPTION"
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
            }
        }
    }

    private fun writeToDatabase(context: Context, intent: Intent) {
        val descriptionResId = intent.getIntExtra(
            EXTRA_NOTIFICATION_DESCRIPTION,
            R.string.unknown_activity_notification_text
        )
        val packageName = intent.getStringExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME)
        val networkType = intent.getIntExtra(EXTRA_NOTIFICATION_NETWORK_TYPE, NETWORK_TYPE_UNKNOWN)
        if (packageName != null) {
            val notification = NotificationInfo(
                descriptionResId = descriptionResId,
                timestamp = Calendar.getInstance().timeInMillis,
                packageName = packageName,
                networkType = networkType
            )

            if (::notificationsDao.isInitialized) {
                coroutineScope.launch {
                    notificationsDao.insert(notification)
                    context.sendBroadcast(Intent(ACTION_NOTIFICATIONS_DATABASE_UPDATED))
                }
            }
        }
    }
}