package com.schugarkub.dataguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.repository.notifications.NotificationsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreferencesBottomSheetViewModel(
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationsRepository.deleteAllNotifications()
        }
    }
}

class PreferencesBottomSheetViewModelFactory @Inject constructor(
    private val notificationsRepository: NotificationsRepository
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PreferencesBottomSheetViewModel::class.java) ->
                PreferencesBottomSheetViewModel(notificationsRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}