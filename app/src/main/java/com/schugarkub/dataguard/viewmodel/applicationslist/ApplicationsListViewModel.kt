package com.schugarkub.dataguard.viewmodel.applicationslist

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.model.ApplicationInfo
import com.schugarkub.dataguard.model.NetworkUsageInfo
import com.schugarkub.dataguard.utils.NetworkUsageRetriever
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*

class ApplicationsListViewModel(application: Application) : ViewModel() {

    private val packageManager = application.packageManager
    private val networkUsageRetriever = NetworkUsageRetriever(application.applicationContext)

    val applicationsLiveData by lazy {
        MutableLiveData<List<ApplicationInfo>>()
    }

    @Suppress("deprecation")
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
            networkUsageRetriever.getNetworkUsageInfo(ConnectivityManager.TYPE_WIFI, startTime, endTime)
        }

        val mobileNetworkUsageDeferred = viewModelScope.async {
            networkUsageRetriever.getNetworkUsageInfo(ConnectivityManager.TYPE_MOBILE, startTime, endTime)
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

            applicationsLiveData.value = applications.sortedBy { it.title }
        }
    }

    private fun getApplications(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(0).map {
            ApplicationInfo(
                packageName = it.packageName,
                uid = it.uid,
                title = it.loadLabel(packageManager).toString(),
                icon = it.loadIcon(packageManager)
            )
        }
    }
}