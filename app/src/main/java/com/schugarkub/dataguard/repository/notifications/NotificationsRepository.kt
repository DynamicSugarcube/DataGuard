package com.schugarkub.dataguard.repository.notifications

import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {

    fun getAllNotificationsFlow(): Flow<List<NotificationInfo>>

    suspend fun addNotification(notification: NotificationInfo): Long

    suspend fun deleteAllNotifications()
}