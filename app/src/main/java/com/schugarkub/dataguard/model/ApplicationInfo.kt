package com.schugarkub.dataguard.model

import android.graphics.drawable.Drawable

data class ApplicationInfo(
    val packageName: String,
    val uid: Int,
    val title: String,
    val icon: Drawable,
    var networkUsageInfo: NetworkUsageInfo = NetworkUsageInfo()
)