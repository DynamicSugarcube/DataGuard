package com.schugarkub.dataguard.model

data class AppPackageInfo(
    val name: String,
    val uid: Int,
    var networkUsageInfo: NetworkUsageInfo = NetworkUsageInfo()
)