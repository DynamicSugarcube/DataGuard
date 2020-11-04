package com.schugarkub.dataguard.model

import java.math.RoundingMode

data class NetworkUsageInfo(
    var rxBytes: Long = 0L,
    var txBytes: Long = 0L
) {
    private val totalBytes: Long
        get() = rxBytes + txBytes

    val formattedRxBytes: String
        get() = formatBytes(rxBytes)
    val formattedTxBytes: String
        get() = formatBytes(txBytes)
    val formattedTotalBytes: String
        get() = formatBytes(totalBytes)

    operator fun plus(increment: NetworkUsageInfo): NetworkUsageInfo {
        return NetworkUsageInfo(
            rxBytes + increment.rxBytes,
            txBytes + increment.txBytes
        )
    }

    private fun formatBytes(bytes: Long): String {
        var formatted = bytes.toDouble()
        var pow = 0
        while (formatted / 1024 >= 1) {
            formatted /= 1024
            pow++
        }
        formatted = formatted.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        return "$formatted ${
            when (pow) {
                0 -> "B"
                1 -> "KB"
                2 -> "MB"
                3 -> "GB"
                else -> "TB"
            }
        }"
    }
}