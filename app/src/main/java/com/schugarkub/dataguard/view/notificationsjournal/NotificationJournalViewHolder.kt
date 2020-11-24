package com.schugarkub.dataguard.view.notificationsjournal

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

    private val notificationDescriptionView = itemView.findViewById<TextView>(R.id.notification_description)
    private val notificationTimestampView = itemView.findViewById<TextView>(R.id.notification_timestamp)
    private val notificationApplicationIconView = itemView.findViewById<ImageView>(R.id.notification_application_icon)
    private val notificationNetworkTypeView = itemView.findViewById<TextView>(R.id.notification_network_type)

    fun bind(notification: NotificationInfo) {
        val appInfo = packageManager.getApplicationInfo(notification.packageName, 0)
        val appLabel = appInfo.loadLabel(packageManager)
        val appImage = appInfo.loadIcon(packageManager)

        val description = itemView.context.getString(notification.descriptionResId, appLabel)

        notificationDescriptionView.text = description
        notificationTimestampView.text = notification.formattedTimestamp
        notificationNetworkTypeView.text = notification.getNetworkTypeString(itemView.context)
        notificationApplicationIconView.setImageDrawable(appImage)
    }

    companion object {

        fun create(parent: ViewGroup): NotificationJournalViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_journal_item, parent, false)
            return NotificationJournalViewHolder(view)
        }
    }
}