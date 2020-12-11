package com.schugarkub.dataguard.repository.notifications

import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_WIFI
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NotificationsRepositoryImplTest {

    private lateinit var repository: NotificationsRepository

    @Before
    fun setUp() {
        val dao = FakeNotificationsDao()
        repository = NotificationsRepositoryImpl(dao)
    }

    @Test
    fun insertValue_checkInsertedValueId() = runBlocking {
        val actual = repository.addNotification(NOTIFICATIONS.first())

        Assert.assertEquals(NOTIFICATIONS.first().id, actual)
    }

    @Test
    fun getAll_emptyDatabase_returnEmptyList() = runBlocking {
        val actual = repository.getAllNotificationsFlow().first()

        Assert.assertEquals(emptyList<NotificationInfo>(), actual)
    }

    @Test
    fun getAll_oneInsertion_returnInsertedValues() = runBlocking {
        repository.addNotification(NOTIFICATIONS.first())

        val actual = repository.getAllNotificationsFlow().first()

        Assert.assertEquals(NOTIFICATIONS.first(), actual.last())
    }

    @Test
    fun getAll_manyInsertions_returnInsertedValues() = runBlocking {
        NOTIFICATIONS.forEach { notification ->
            repository.addNotification(notification)
        }

        val actual = repository.getAllNotificationsFlow().first()

        Assert.assertEquals(NOTIFICATIONS, actual)
    }

    @Test
    fun cleanAfterManyInsertions_returnEmptyList() = runBlocking {
        NOTIFICATIONS.forEach { notification ->
            repository.addNotification(notification)
        }

        repository.deleteAllNotifications()

        val actual = repository.getAllNotificationsFlow().first()

        Assert.assertEquals(emptyList<NotificationInfo>(), actual)
    }

    companion object {
        private const val NUM_OF_NOTIFICATIONS = 5
        private val NOTIFICATIONS = List(NUM_OF_NOTIFICATIONS) { index ->
            NotificationInfo(
                id = index.toLong(),
                descriptionResId = index,
                timestamp = System.currentTimeMillis(),
                packageName = "package$index",
                networkType = NETWORK_TYPE_WIFI
            )
        }
    }
}