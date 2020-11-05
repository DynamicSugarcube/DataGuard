package com.schugarkub.dataguard.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BaseViewModelFactory(private val application: Application) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ApplicationsListViewModel::class.java) ->
                ApplicationsListViewModel(application) as T
            modelClass.isAssignableFrom(NotificationJournalViewModel::class.java) ->
                NotificationJournalViewModel(application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}