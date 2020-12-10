package com.schugarkub.dataguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepository
import com.schugarkub.dataguard.utils.percentToFloat
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel(private val repository: ApplicationSettingsRepository) : ViewModel() {

    val thresholdLiveData =
        repository.getBytesThresholdFlow().asLiveData(viewModelScope.coroutineContext)
    val maxBytesRateDeviationLiveData =
        repository.getMaxBytesRateDeviationFlow().asLiveData(viewModelScope.coroutineContext)
    val learningIterationsLiveData =
        repository.getLearningIterationsFlow().asLiveData(viewModelScope.coroutineContext)

    val onThresholdChangedCallback = { value: Any ->
        try {
            val threshold = value.toString().toLong()
            viewModelScope.launch {
                repository.updateThreshold(threshold)
            }
            true
        } catch (e: NumberFormatException) {
            Timber.w(e, "Couldn't update threshold")
            false
        }
    }

    val onMaxBytesRateDeviationChangedCallback = { value: Any ->
        try {
            val percent = value.toString().toInt()
            val deviation = percentToFloat(percent)
            viewModelScope.launch {
                repository.updateMaxBytesRateDeviation(deviation)
            }
            true
        } catch (e: NumberFormatException) {
            Timber.w(e, "Couldn't update max bytes rate deviation")
            false
        }
    }

    val onLearningIterationsChangedCallback = { value: Any ->
        try {
            val iterations = value.toString().toInt()
            viewModelScope.launch {
                repository.updateLearningIterations(iterations)
            }
            true
        } catch (e: NumberFormatException) {
            Timber.w(e, "Couldn't update min calibration times")
            false
        }
    }
}

class SettingsViewModelFactory @Inject constructor(
    private val repository: ApplicationSettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}