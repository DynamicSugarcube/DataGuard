package com.schugarkub.dataguard.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.schugarkub.dataguard.database.applicationsettings.APPLICATION_SETTINGS_DATABASE_NAME

@Entity(tableName = APPLICATION_SETTINGS_DATABASE_NAME)
data class ApplicationSettings(

    @PrimaryKey
    val id: Int = 0,

    @ColumnInfo(name = "threshold")
    var bytesThreshold: Long = DEFAULT_TX_BYTES_THRESHOLD,

    @ColumnInfo(name = "max_bytes_rate_deviation")
    var maxBytesRateDeviation: Float = DEFAULT_MAX_BYTES_RATE_DEVIATION,

    @ColumnInfo(name = "min_calibration_times")
    var minCalibrationTimes: Int = DEFAULT_MIN_CALIBRATION_TIMES,

    @ColumnInfo(name = "max_calibration_times")
    var maxCalibrationTimes: Int = DEFAULT_MAX_CALIBRATION_TIMES
) {

    companion object {

        const val DEFAULT_TX_BYTES_THRESHOLD = 10_000L
        const val DEFAULT_MAX_BYTES_RATE_DEVIATION = 0.5F
        const val DEFAULT_MIN_CALIBRATION_TIMES = 100
        const val DEFAULT_MAX_CALIBRATION_TIMES = 10_000
    }
}