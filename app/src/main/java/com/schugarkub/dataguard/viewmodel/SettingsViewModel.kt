package com.schugarkub.dataguard.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepository
import com.schugarkub.dataguard.repository.networkusage.NetworkUsageRepository
import com.schugarkub.dataguard.utils.percentToFloat
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel(
    private val applicationSettingsRepository: ApplicationSettingsRepository,
    private val networkUsageRepository: NetworkUsageRepository
) : ViewModel() {

    val thresholdLiveData = applicationSettingsRepository
        .getBytesThresholdFlow().asLiveData(viewModelScope.coroutineContext)
    val maxBytesRateDeviationLiveData = applicationSettingsRepository
        .getMaxBytesRateDeviationFlow().asLiveData(viewModelScope.coroutineContext)
    val learningIterationsLiveData = applicationSettingsRepository
        .getLearningIterationsFlow().asLiveData(viewModelScope.coroutineContext)

    fun onResetStats(context: Context) {
        viewModelScope.launch {
            networkUsageRepository.deleteAllEntities()
            Toast.makeText(context, R.string.reset_network_usage_string, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun onResetSettings(context: Context) {
        viewModelScope.launch {
            applicationSettingsRepository.resetSettings()
            Toast.makeText(context, R.string.reset_settings_string, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun onThresholdChanged(value: Any): Boolean {
        return try {
            val threshold = value.toString().toLong()
            viewModelScope.launch {
                applicationSettingsRepository.updateThreshold(threshold)
            }
            true
        } catch (e: NumberFormatException) {
            Timber.w(e, "Couldn't update threshold")
            false
        }
    }

    fun onMaxBytesRateDeviationChanged(value: Any): Boolean {
        return try {
            val percent = value.toString().toInt()
            val deviation = percentToFloat(percent)
            viewModelScope.launch {
                applicationSettingsRepository.updateMaxBytesRateDeviation(deviation)
            }
            true
        } catch (e: NumberFormatException) {
            Timber.w(e, "Couldn't update max bytes rate deviation")
            false
        }
    }

    fun onLearningIterationsChanged(value: Any): Boolean {
        return try {
            val iterations = value.toString().toInt()
            viewModelScope.launch {
                applicationSettingsRepository.updateLearningIterations(iterations)
            }
            true
        } catch (e: NumberFormatException) {
            Timber.w(e, "Couldn't update min calibration times")
            false
        }
    }
}

class SettingsViewModelFactory @Inject constructor(
    private val applicationSettingsRepository: ApplicationSettingsRepository,
    private val networkUsageRepository: NetworkUsageRepository
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(applicationSettingsRepository, networkUsageRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}