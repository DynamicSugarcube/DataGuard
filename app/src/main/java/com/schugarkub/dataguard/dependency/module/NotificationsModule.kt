package com.schugarkub.dataguard.dependency.module

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import com.schugarkub.dataguard.database.notifications.NotificationsDao
import com.schugarkub.dataguard.database.notifications.NotificationsDatabase
import com.schugarkub.dataguard.helpers.notifications.NotificationSender
import com.schugarkub.dataguard.repository.notifications.NotificationsRepository
import com.schugarkub.dataguard.repository.notifications.NotificationsRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class NotificationsModule {

    @Provides
    fun provideNotificationSender(
        context: Context,
        packageManager: PackageManager,
        notificationManager: NotificationManager,
        notificationsRepository: NotificationsRepository
    ): NotificationSender {
        return NotificationSender(
            context,
            packageManager,
            notificationManager,
            notificationsRepository
        )
    }

    @Provides
    fun provideNotificationsDao(application: Application): NotificationsDao {
        return NotificationsDatabase.getInstance(application.applicationContext).dao
    }

    @Provides
    fun provideNotificationsRepository(dao: NotificationsDao): NotificationsRepository {
        return NotificationsRepositoryImpl(dao)
    }
}