package com.schugarkub.dataguard.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.schugarkub.dataguard.database.networkusage.NETWORK_USAGE_DATABASE_NAME

private const val MAX_CALIBRATION_TIMES = 10_000

@Entity(tableName = NETWORK_USAGE_DATABASE_NAME)
data class NetworkUsageEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "average_rx")
    var averageRxBytesRate: Long = 0L,

    @ColumnInfo(name = "average_tx")
    var averageTxBytesRate: Long = 0L,

    @ColumnInfo(name = "rx_calibration_times")
    var rxCalibrationTimes: Int = 1,

    @ColumnInfo(name = "tx_calibration_times")
    var txCalibrationTimes: Int = 1
) {

    fun updateAverageRxBytesRate(newRate: Long) {
        updateAverageBytesRate(newRate, BytesType.RX)
    }

    fun updateAverageTxBytesRate(newRate: Long) {
        updateAverageBytesRate(newRate, BytesType.TX)
    }

    private fun updateAverageBytesRate(newRate: Long, type: BytesType) {
        if (newRate > 0) {
            when (type) {
                BytesType.RX -> {
                    if (rxCalibrationTimes < MAX_CALIBRATION_TIMES) {
                        val total = rxCalibrationTimes * averageRxBytesRate + newRate
                        rxCalibrationTimes++
                        averageRxBytesRate = total / rxCalibrationTimes
                    }
                }
                BytesType.TX -> {
                    if (txCalibrationTimes < MAX_CALIBRATION_TIMES) {
                        val total = txCalibrationTimes * averageTxBytesRate + newRate
                        txCalibrationTimes++
                        averageTxBytesRate = total / txCalibrationTimes
                    }
                }
            }
        }
    }

    private enum class BytesType { RX, TX }
}