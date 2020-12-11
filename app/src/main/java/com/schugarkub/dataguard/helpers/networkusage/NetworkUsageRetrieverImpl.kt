package com.schugarkub.dataguard.helpers.networkusage

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.os.RemoteException
import com.schugarkub.dataguard.model.NetworkUsageInfo
import timber.log.Timber
import javax.inject.Inject

class NetworkUsageRetrieverImpl @Inject constructor(
    private val manager: NetworkStatsManager
) : NetworkUsageRetriever {

    override fun getNetworkUsageInfo(
        networkType: Int,
        startTime: Long,
        endTime: Long
    ): Map<Int, NetworkUsageInfo> {
        val networkUsage = mutableMapOf<Int, NetworkUsageInfo>()

        try {
            val networkStats =
                manager.querySummary(networkType, null, startTime, endTime)

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