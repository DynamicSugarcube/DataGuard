package com.schugarkub.dataguard.view.applicationslist

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schugarkub.dataguard.model.AppPackageInfo

class ApplicationsListAdapter : RecyclerView.Adapter<ApplicationsListViewHolder>() {

    var applications = emptyList<AppPackageInfo>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationsListViewHolder {
        return ApplicationsListViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ApplicationsListViewHolder, position: Int) {
        holder.bind(applications[position])
    }

    override fun getItemCount(): Int {
        return applications.size
    }
}