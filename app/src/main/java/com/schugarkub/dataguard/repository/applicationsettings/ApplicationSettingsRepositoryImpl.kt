package com.schugarkub.dataguard.repository.applicationsettings

import com.schugarkub.dataguard.database.applicationsettings.ApplicationSettingsDao
import com.schugarkub.dataguard.model.ApplicationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationSettingsRepositoryImpl @Inject constructor(
    private val dao: ApplicationSettingsDao
) : ApplicationSettingsRepository {

    override fun getBytesThresholdFlow(): Flow<Long> {
        val settingsFlow = dao.getSettingsFlow()
        return settingsFlow.map { settings ->
            settings?.bytesThreshold ?: ApplicationSettings.DEFAULT_TX_BYTES_THRESHOLD
        }
    }

    override fun getMaxBytesRateDeviationFlow(): Flow<Float> {
        val settingsFlow = dao.getSettingsFlow()
        return settingsFlow.map { settings ->
            settings?.maxBytesRateDeviation ?: ApplicationSettings.DEFAULT_MAX_BYTES_RATE_DEVIATION
        }
    }

    override fun getLearningIterationsFlow(): Flow<Int> {
        val settingsFlow = dao.getSettingsFlow()
        return settingsFlow.map { settings ->
            settings?.learningIterations ?: ApplicationSettings.DEFAULT_LEARNING_ITERATIONS
        }
    }

    override suspend fun updateThreshold(threshold: Long) {
        var settings = dao.getSettings()
        if (settings == null) {
            settings = ApplicationSettings(bytesThreshold = threshold)
            dao.insert(settings)
        } else {
            settings.bytesThreshold = threshold
            dao.update(settings)
        }
    }

    override suspend fun updateMaxBytesRateDeviation(deviation: Float) {
        var settings = dao.getSettings()
        if (settings == null) {
            settings = ApplicationSettings(maxBytesRateDeviation = deviation)
            dao.insert(settings)
        } else {
            settings.maxBytesRateDeviation = deviation
            dao.update(settings)
        }
    }

    override suspend fun updateLearningIterations(iterations: Int) {
        var settings = dao.getSettings()
        if (settings == null) {
            settings = ApplicationSettings(learningIterations = iterations)
            dao.insert(settings)
        } else {
            settings.learningIterations = iterations
            dao.update(settings)
        }
    }

    override suspend fun resetSettings() {
        val defaultSettings = ApplicationSettings()
        dao.update(defaultSettings)
    }
}