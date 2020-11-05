package com.schugarkub.dataguard.view.notificationsjournal

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.model.NotificationInfo

class NotificationJournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val packageManager = itemView.context.packageManager

    private val notificationTitleView = itemView.findViewById<TextView>(R.id.notification_title)
    private val notificationTimestampView = itemView.findViewById<TextView>(R.id.notification_timestamp)
    private val notificationApplicationIconView = itemView.findViewById<ImageView>(R.id.notification_application_icon)
    private val notificationNetworkTypeView = itemView.findViewById<TextView>(R.id.notification_network_type)

    fun bind(notification: NotificationInfo) {
        notificationTitleView.text = notification.title
        notificationTimestampView.text = notification.timestamp
        notificationNetworkTypeView.text = notification.networkType
        notificationApplicationIconView.setImageDrawable(
            getApplicationIconDrawable(notification.packageName)
        )
    }

    private fun getApplicationIconDrawable(packageName: String): Drawable {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        return applicationInfo.loadIcon(packageManager)
    }

    companion object {

        fun create(parent: ViewGroup): NotificationJournalViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_journal_item, parent, false)
            return NotificationJournalViewHolder(view)
        }
    }
}