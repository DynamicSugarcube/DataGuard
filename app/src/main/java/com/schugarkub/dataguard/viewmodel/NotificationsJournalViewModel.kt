package com.schugarkub.dataguard.viewmodel

import androidx.lifecycle.*
import com.schugarkub.dataguard.repository.notifications.NotificationsRepository
import javax.inject.Inject

class NotificationsJournalViewModel(repository: NotificationsRepository) : ViewModel() {

    val notificationsLiveData = repository.getAllNotificationsFlow().asLiveData()
}

class NotificationsJournalViewModelFactory @Inject constructor(
    private val repository: NotificationsRepository
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotificationsJournalViewModel::class.java) ->
                NotificationsJournalViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}