package com.schugarkub.dataguard.view.applicationslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.model.ApplicationInfo

class ApplicationsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val applicationIconView = itemView.findViewById<ImageView>(R.id.application_icon)
    private val applicationTitleView = itemView.findViewById<TextView>(R.id.application_title)
    private val applicationRxBytesView = itemView.findViewById<TextView>(R.id.application_rx_bytes)
    private val applicationTxBytesView = itemView.findViewById<TextView>(R.id.application_tx_bytes)
    private val applicationTotalBytesView = itemView.findViewById<TextView>(R.id.application_total_bytes)

    fun bind(application: ApplicationInfo) {
        applicationIconView.setImageDrawable(application.icon)
        applicationTitleView.text = application.title
        applicationRxBytesView.text = application.networkUsageInfo.formattedRxBytes
        applicationTxBytesView.text = application.networkUsageInfo.formattedTxBytes
        applicationTotalBytesView.text = application.networkUsageInfo.formattedTotalBytes
    }

    companion object {

        fun create(parent: ViewGroup): ApplicationsListViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.applications_list_item, parent, false)
            return ApplicationsListViewHolder(view)
        }
    }
}