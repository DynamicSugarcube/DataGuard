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

    @ColumnInfo(name = "learning_iterations_times")
    var learningIterations: Int = DEFAULT_LEARNING_ITERATIONS
) {

    companion object {

        const val DEFAULT_TX_BYTES_THRESHOLD = 10_000L
        const val DEFAULT_MAX_BYTES_RATE_DEVIATION = 0.5F
        const val DEFAULT_LEARNING_ITERATIONS = 100
    }
}