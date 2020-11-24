package com.schugarkub.dataguard.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_WIFI
import com.schugarkub.dataguard.model.AppPackageInfo
import com.schugarkub.dataguard.model.NetworkUsageInfo
import com.schugarkub.dataguard.utils.NetworkUsageRetriever
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*

class ApplicationsListViewModel(application: Application) : ViewModel() {

    private val packageManager = application.packageManager
    private val networkUsageRetriever = NetworkUsageRetriever(application.applicationContext)

    val applicationsLiveData by lazy {
        MutableLiveData<List<AppPackageInfo>>()
    }

    fun syncApplications() {
        // Today midnight
        val startTime = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endTime = Calendar.getInstance().timeInMillis

        val applicationsDeferred = viewModelScope.async {
            getApplications()
        }

        val wifiNetworkUsageDeferred = viewModelScope.async {
            networkUsageRetriever.getNetworkUsageInfo(NETWORK_TYPE_WIFI, startTime, endTime)
        }

        val mobileNetworkUsageDeferred = viewModelScope.async {
            networkUsageRetriever.getNetworkUsageInfo(NETWORK_TYPE_MOBILE, startTime, endTime)
        }

        runBlocking {
            val wifiNetworkUsage = wifiNetworkUsageDeferred.await()
            val mobileNetworkUsage = mobileNetworkUsageDeferred.await()

            val applications = applicationsDeferred.await()

            if (wifiNetworkUsage.isNotEmpty() || mobileNetworkUsage.isNotEmpty()) {
                val totalNetworkUsage =
                    (wifiNetworkUsage.asSequence() + mobileNetworkUsage.asSequence())
                        .distinct()
                        .groupBy({ it.key }, { it.value })
                        .mapValues { (_, values) ->
                            var sum = NetworkUsageInfo()
                            values.forEach {
                                sum += it
                            }
                            sum
                        }

                applications.forEach { app ->
                    if (totalNetworkUsage.contains(app.uid)) {
                        totalNetworkUsage[app.uid]?.let { info ->
                            app.networkUsageInfo = info
                        }
                    }
                }
            }

            applicationsLiveData.value =
                applications.sortedBy { it.networkUsageInfo.totalBytes }.reversed()
        }
    }

    private fun getApplications(): List<AppPackageInfo> {
        return packageManager.getInstalledApplications(0).map {
            AppPackageInfo(
                name = it.packageName,
                uid = it.uid
            )
        }
    }
}