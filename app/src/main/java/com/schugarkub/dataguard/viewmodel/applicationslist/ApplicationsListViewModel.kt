package com.schugarkub.dataguard.viewmodel.applicationslist

import android.app.Application
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.RemoteException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.model.ApplicationInfo
import com.schugarkub.dataguard.model.NetworkUsageInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

class ApplicationsListViewModel(application: Application) : ViewModel() {

    private val packageManager = application.packageManager
    private val networkStatsManager = application.getSystemService(Context.NETWORK_STATS_SERVICE)
            as NetworkStatsManager

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
            getNetworkUsageInfo(ConnectivityManager.TYPE_WIFI, startTime, endTime)
        }

        val mobileNetworkUsageDeferred = viewModelScope.async {
            getNetworkUsageInfo(ConnectivityManager.TYPE_MOBILE, startTime, endTime)
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

    private fun getNetworkUsageInfo(
        networkType: Int,
        startTime: Long,
        endTime: Long
    ): Map<Int, NetworkUsageInfo> {
        val networkUsage = mutableMapOf<Int, NetworkUsageInfo>()

        try {
            val networkStats =
                networkStatsManager.querySummary(networkType, null, startTime, endTime)

            val bucket = NetworkStats.Bucket()
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket)

                val uid = bucket.uid
                val rxBytes = bucket.rxBytes
                val txBytes = bucket.txBytes

                if (networkUsage.contains(uid)) {
                    networkUsage[uid]?.let {
                        it.rxBytes += rxBytes
                        it.txBytes += txBytes
                    }
                } else {
                    networkUsage[uid] = NetworkUsageInfo(rxBytes, txBytes)
                }
            }

            networkStats.close()
        } catch (e: RemoteException) {
            Timber.e(e, "Couldn't query network stats")
        } catch (e: SecurityException) {
            Timber.e(e, "Couldn't query network stats")
        }

        return networkUsage
    }
}