package com.schugarkub.dataguard.repository.applicationsettings

import kotlinx.coroutines.flow.Flow

interface ApplicationSettingsRepository {

    fun getBytesThresholdFlow(): Flow<Long>
    fun getMaxBytesRateDeviationFlow(): Flow<Float>
    fun getLearningIterationsFlow(): Flow<Int>

    suspend fun updateThreshold(threshold: Long)
    suspend fun updateMaxBytesRateDeviation(deviation: Float)
    suspend fun updateLearningIterations(iterations: Int)

    suspend fun resetSettings()
}