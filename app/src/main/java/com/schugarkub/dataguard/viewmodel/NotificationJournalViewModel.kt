package com.schugarkub.dataguard.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schugarkub.dataguard.database.NotificationsDatabase
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.launch

class NotificationJournalViewModel(application: Application) : ViewModel() {

    private val context = application.applicationContext

    private val notificationsDao = NotificationsDatabase.getInstance(context).dao

    val notificationsLiveData by lazy {
        MutableLiveData<List<NotificationInfo>>()
    }

    fun syncNotifications() {
        viewModelScope.launch {
            val notifications = notificationsDao.getAll()
            notificationsLiveData.value = notifications
        }
    }
}