package com.schugarkub.dataguard.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.database.applicationsettings.ApplicationSettingsDatabase
import com.schugarkub.dataguard.database.applicationsettings.ApplicationSettingsRepositoryImpl
import com.schugarkub.dataguard.utils.percentToFloat
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsViewModel(application: Application) : ViewModel() {

    private val dao = ApplicationSettingsDatabase.getInstance(application.applicationContext).dao
    private val repository = ApplicationSettingsRepositoryImpl(dao)

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