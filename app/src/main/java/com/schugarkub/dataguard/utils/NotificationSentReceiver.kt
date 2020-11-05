package com.schugarkub.dataguard.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.schugarkub.dataguard.model.NotificationInfo
import timber.log.Timber

const val ACTION_NOTIFICATION_SENT = "com.schugarkub.dataguard.action.NOTIFICATION_SENT"

const val EXTRA_NOTIFICATION_TITLE = "com.schugarkub.dataguard.extra.NOTIFICATION_TITLE"
const val EXTRA_NOTIFICATION_TIMESTAMP = "com.schugarkub.dataguard.extra.NOTIFICATION_TIMESTAMP"
const val EXTRA_NOTIFICATION_APP_PACKAGE_NAME = "com.schugarkub.dataguard.extra.NOTIFICATION_APP_PACKAGE_NAME"
const val EXTRA_NOTIFICATION_NETWORK_TYPE = "com.schugarkub.dataguard.extra.NOTIFICATION_NETWORK_TYPE"

class NotificationSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val title = it.getStringExtra(EXTRA_NOTIFICATION_TITLE)
            val timestamp = it.getStringExtra(EXTRA_NOTIFICATION_TIMESTAMP)
            val packageName = it.getStringExtra(EXTRA_NOTIFICATION_APP_PACKAGE_NAME)
            val networkType = it.getStringExtra(EXTRA_NOTIFICATION_NETWORK_TYPE)
            if (title != null && timestamp != null && packageName != null && networkType != null) {
                val notification = NotificationInfo(title, timestamp, packageName, networkType)
                Timber.d("Received notification %s", notification)
            }
        }
    }
}