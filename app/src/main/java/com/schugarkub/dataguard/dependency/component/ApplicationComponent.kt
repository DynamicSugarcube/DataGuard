package com.schugarkub.dataguard.dependency.component

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import com.schugarkub.dataguard.dependency.module.ApplicationModule
import com.schugarkub.dataguard.dependency.module.ApplicationSettingsModule
import com.schugarkub.dataguard.dependency.module.NetworkUsageModule
import com.schugarkub.dataguard.dependency.module.NotificationsModule
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetriever
import com.schugarkub.dataguard.helpers.notifications.NotificationSender
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepository
import com.schugarkub.dataguard.repository.networkusage.NetworkUsageRepository
import com.schugarkub.dataguard.repository.notifications.NotificationsRepository
import dagger.Component

@Component(modules = [
    ApplicationModule::class,
    ApplicationSettingsModule::class,
    NotificationsModule::class,
    NetworkUsageModule::class
])
interface ApplicationComponent {

    fun getApplication(): Application

    fun getApplicationContext(): Context

    fun getPackageManager(): PackageManager

    fun getNotificationManager(): NotificationManager

    fun getNetworkUsageRetriever(): NetworkUsageRetriever

    fun getNetworkUsageRepository(): NetworkUsageRepository

    fun getApplicationSettingsRepository(): ApplicationSettingsRepository

    fun getNotificationSender(): NotificationSender

    fun getNotificationsRepository(): NotificationsRepository
}