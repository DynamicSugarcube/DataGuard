package com.schugarkub.dataguard.view.notificationsjournal

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.model.NotificationInfo

class NotificationJournalAdapter : RecyclerView.Adapter<NotificationJournalViewHolder>() {

    var notifications = emptyList<NotificationInfo>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationJournalViewHolder {
        return NotificationJournalViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: NotificationJournalViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}