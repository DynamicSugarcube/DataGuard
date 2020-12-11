package com.schugarkub.dataguard.dependency.module

import android.app.Application
import android.app.NotificationManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    fun provideApplication(): Application {
        return application
    }

    @Provides
    fun providePackageManager(): PackageManager {
        return application.packageManager
    }

    @Provides
    fun provideNotificationManager(): NotificationManager {
        return application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun provideNetworkStatsManager(): NetworkStatsManager {
        return application.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    }
}