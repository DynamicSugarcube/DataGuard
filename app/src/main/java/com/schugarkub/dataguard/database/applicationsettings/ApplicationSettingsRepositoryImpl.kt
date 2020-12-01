package com.schugarkub.dataguard.database.applicationsettings

import com.schugarkub.dataguard.model.ApplicationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ApplicationSettingsRepositoryImpl(private val dao: ApplicationSettingsDao) :
    ApplicationSettingsRepository {

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

    override fun getMinCalibrationTimesFlow(): Flow<Int> {
        val settingsFlow = dao.getSettingsFlow()
        return settingsFlow.map { settings ->
            settings?.minCalibrationTimes ?: ApplicationSettings.DEFAULT_MIN_CALIBRATION_TIMES
        }
    }

    override fun getMaxCalibrationTimesFlow(): Flow<Int> {
        val settingsFlow = dao.getSettingsFlow()
        return settingsFlow.map { settings ->
            settings?.maxCalibrationTimes ?: ApplicationSettings.DEFAULT_MAX_CALIBRATION_TIMES
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

    override suspend fun updateMinCalibrationTimes(times: Int) {
        var settings = dao.getSettings()
        if (settings == null) {
            settings = ApplicationSettings(minCalibrationTimes = times)
            dao.insert(settings)
        } else {
            settings.minCalibrationTimes = times
            dao.update(settings)
        }
    }

    override suspend fun updateMaxCalibrationTimes(times: Int) {
        var settings = dao.getSettings()
        if (settings == null) {
            settings = ApplicationSettings(maxCalibrationTimes = times)
            dao.insert(settings)
        } else {
            settings.maxCalibrationTimes = times
            dao.update(settings)
        }
    }

    override suspend fun resetSettings() {
        val defaultSettings = ApplicationSettings()
        dao.update(defaultSettings)
    }
}