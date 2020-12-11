package com.schugarkub.dataguard.helpers.networkusage

import com.schugarkub.dataguard.model.NetworkUsageInfo

interface NetworkUsageRetriever {

    fun getNetworkUsageInfo(
        networkType: Int,
        startTime: Long,
        endTime: Long
    ): Map<Int, NetworkUsageInfo>
}