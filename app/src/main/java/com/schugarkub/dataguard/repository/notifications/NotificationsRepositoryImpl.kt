package com.schugarkub.dataguard.repository.notifications

import com.schugarkub.dataguard.database.notifications.NotificationsDao
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val dao: NotificationsDao
) : NotificationsRepository {

    override fun getAllNotificationsFlow(): Flow<List<NotificationInfo>> {
        val notificationsFlow = dao.getAllFlow()
        return notificationsFlow.map { notifications ->
            notifications ?: emptyList()
        }
    }

    override suspend fun addNotification(notification: NotificationInfo): Long {
        return dao.insert(notification)
    }

    override suspend fun deleteAllNotifications() {
        dao.clean()
    }
}