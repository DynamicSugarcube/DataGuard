package com.schugarkub.dataguard.view.applicationslist

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.model.AppPackageInfo

class ApplicationsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val packageManager = itemView.context.packageManager

    private val applicationIconView = itemView.findViewById<ImageView>(R.id.application_icon)
    private val applicationTitleView = itemView.findViewById<TextView>(R.id.application_title)
    private val applicationRxBytesView = itemView.findViewById<TextView>(R.id.application_rx_bytes)
    private val applicationTxBytesView = itemView.findViewById<TextView>(R.id.application_tx_bytes)
    private val applicationTotalBytesView = itemView.findViewById<TextView>(R.id.application_total_bytes)

    fun bind(appPackageInfo: AppPackageInfo) {
        val appInfo = packageManager.getApplicationInfo(appPackageInfo.name, 0)
        val appLabel = appInfo.loadLabel(packageManager)
        val appIcon = appInfo.loadIcon(packageManager)

        applicationIconView.setImageDrawable(appIcon)
        applicationTitleView.text = appLabel
        applicationRxBytesView.text = appPackageInfo.networkUsageInfo.formattedRxBytes
        applicationTxBytesView.text = appPackageInfo.networkUsageInfo.formattedTxBytes
        applicationTotalBytesView.text = appPackageInfo.networkUsageInfo.formattedTotalBytes

        itemView.setOnClickListener {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                intent.data = Uri.parse("package:${appPackageInfo.name}")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                itemView.context.startActivity(intent)
            }
        }
    }

    companion object {

        fun create(parent: ViewGroup): ApplicationsListViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.applications_list_item, parent, false)
            return ApplicationsListViewHolder(view)
        }
    }
}