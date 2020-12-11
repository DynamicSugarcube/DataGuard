package com.schugarkub.dataguard.repository.notifications

import com.schugarkub.dataguard.database.notifications.NotificationsDao
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeNotificationsDao : NotificationsDao {

    private val notifications = mutableListOf<NotificationInfo>()

    override suspend fun insert(notification: NotificationInfo): Long {
        notifications.add(notification)
        return notifications.last().id
    }

    override fun getAllFlow(): Flow<List<NotificationInfo>?> {
        return flow {
            emit(notifications)
        }
    }

    override suspend fun clean() {
        notifications.clear()
    }
}