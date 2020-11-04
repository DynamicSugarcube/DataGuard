package com.schugarkub.dataguard.viewmodel.applicationslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.model.ApplicationInfo
import kotlinx.coroutines.launch
import timber.log.Timber

class ApplicationsListViewModel(application: Application) : ViewModel() {

    private val packageManager = application.packageManager

    val applicationsLiveData by lazy {
        MutableLiveData<List<ApplicationInfo>>()
    }

    fun syncApplications() {
        viewModelScope.launch {
            val applications = packageManager.getInstalledApplications(0).map {
                ApplicationInfo(
                    it.loadLabel(packageManager).toString(),
                    it.loadIcon(packageManager)
                )
            }
            applicationsLiveData.value = applications.sortedBy { it.title }
        }
    }
}