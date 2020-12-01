package com.schugarkub.dataguard.database.applicationsettings

import kotlinx.coroutines.flow.Flow

interface ApplicationSettingsRepository {

    fun getBytesThresholdFlow(): Flow<Long>
    fun getMaxBytesRateDeviationFlow(): Flow<Float>
    fun getMinCalibrationTimesFlow(): Flow<Int>
    fun getMaxCalibrationTimesFlow(): Flow<Int>

    suspend fun updateThreshold(threshold: Long)
    suspend fun updateMaxBytesRateDeviation(deviation: Float)
    suspend fun updateMinCalibrationTimes(times: Int)
    suspend fun updateMaxCalibrationTimes(times: Int)

    suspend fun resetSettings()
}