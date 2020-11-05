package com.schugarkub.dataguard.viewmodel.notificationjournal

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.model.NotificationInfo
import java.text.DateFormat
import java.util.*

class NotificationJournalViewModel(application: Application) : ViewModel() {

    private val context = application.applicationContext

    val notificationsLiveData by lazy {
        MutableLiveData<List<NotificationInfo>>()
    }

    fun syncNotifications() {
        // TODO Sync notifications from database
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

        val title = context.getString(R.string.threshold_reached_notification_title)
        val timestamp = dateFormat.format(Calendar.getInstance().time).toString()
        val packageName = "com.vkontakte.android"
        val networkType = "Mobile"
        val notification = NotificationInfo(title, timestamp, packageName, networkType)

        val notifications = List(5) { notification }
        notificationsLiveData.value = notifications
    }
}