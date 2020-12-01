package com.schugarkub.dataguard.database.applicationsettings

import com.schugarkub.dataguard.model.ApplicationSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before

import org.junit.Test

class ApplicationSettingsRepositoryImplTest {

    private lateinit var repository: ApplicationSettingsRepository

    @Before
    fun setUp() {
        val dao = FakeApplicationSettingsDao()
        repository = ApplicationSettingsRepositoryImpl(dao)
    }

    @Test
    fun getBytesThreshold_emptyDatabase_returnDefault() = runBlocking {
        val actual = repository.getBytesThresholdFlow().first()

        Assert.assertEquals(ApplicationSettings.DEFAULT_TX_BYTES_THRESHOLD, actual)
    }

    @Test
    fun getBytesThreshold_updateOnce_returnNewValue() = runBlocking {
        val value = 100_000L
        repository.updateThreshold(value)

        val actual = repository.getBytesThresholdFlow().first()

        Assert.assertEquals(value, actual)
    }

    @Test
    fun getBytesThreshold_updateSeveralTimes_returnLastValue() = runBlocking {
        val values = listOf(100_000L, 200_000L, 300_000L)
        values.forEach { value ->
            repository.updateThreshold(value)
        }

        val actual = repository.getBytesThresholdFlow().first()

        Assert.assertEquals(values.last(), actual)
    }

    @Test
    fun getMaxBytesRateDeviation_emptyDatabase_returnDefault() = runBlocking {
        val actual = repository.getMaxBytesRateDeviationFlow().first()

        Assert.assertEquals(ApplicationSettings.DEFAULT_MAX_BYTES_RATE_DEVIATION, actual)
    }

    @Test
    fun getMaxBytesRateDeviation_updateOnce_returnNewValue() = runBlocking {
        val value = 0.1F
        repository.updateMaxBytesRateDeviation(value)

        val actual = repository.getMaxBytesRateDeviationFlow().first()

        Assert.assertEquals(value, actual)
    }

    @Test
    fun getMaxBytesRateDeviation_updateSeveralTimes_returnLastValue() = runBlocking {
        val values = listOf(0.1F, 0.2F, 0.3F)
        values.forEach { value ->
            repository.updateMaxBytesRateDeviation(value)
        }

        val actual = repository.getMaxBytesRateDeviationFlow().first()

        Assert.assertEquals(values.last(), actual)
    }

    @Test
    fun getMinCalibrationTimes_emptyDatabase_returnDefault() = runBlocking {
        val actual = repository.getMinCalibrationTimesFlow().first()

        Assert.assertEquals(ApplicationSettings.DEFAULT_MIN_CALIBRATION_TIMES, actual)
    }

    @Test
    fun getMinCalibrationTimes_updateOnce_returnNewValue() = runBlocking {
        val value = 100
        repository.updateMinCalibrationTimes(value)

        val actual = repository.getMinCalibrationTimesFlow().first()

        Assert.assertEquals(value, actual)
    }

    @Test
    fun getMinCalibrationTimes_updateSeveralTimes_returnLastValue() = runBlocking {
        val values = listOf(100, 200, 300)
        values.forEach { value ->
            repository.updateMinCalibrationTimes(value)
        }

        val actual = repository.getMinCalibrationTimesFlow().first()

        Assert.assertEquals(values.last(), actual)
    }

    @Test
    fun getMaxCalibrationTimes_emptyDatabase_returnDefault() = runBlocking {
        val actual = repository.getMaxCalibrationTimesFlow().first()

        Assert.assertEquals(ApplicationSettings.DEFAULT_MAX_CALIBRATION_TIMES, actual)
    }

    @Test
    fun getMaxCalibrationTimes_updateOnce_returnNewValue() = runBlocking {
        val value = 10_000
        repository.updateMaxCalibrationTimes(value)

        val actual = repository.getMaxCalibrationTimesFlow().first()

        Assert.assertEquals(value, actual)
    }

    @Test
    fun getMaxCalibrationTimes_updateSeveralTimes_returnLastValue() = runBlocking {
        val values = listOf(10_000, 20_000, 30_000)
        values.forEach { value ->
            repository.updateMaxCalibrationTimes(value)
        }

        val actual = repository.getMaxCalibrationTimesFlow().first()

        Assert.assertEquals(values.last(), actual)
    }
}