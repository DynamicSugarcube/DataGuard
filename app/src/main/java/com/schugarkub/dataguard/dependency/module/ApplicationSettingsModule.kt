package com.schugarkub.dataguard.dependency.module

import android.app.Application
import com.schugarkub.dataguard.database.applicationsettings.ApplicationSettingsDao
import com.schugarkub.dataguard.database.applicationsettings.ApplicationSettingsDatabase
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepository
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class ApplicationSettingsModule {

    @Provides
    fun provideApplicationSettingsDao(
        application: Application
    ): ApplicationSettingsDao {
        return ApplicationSettingsDatabase.getInstance(application.applicationContext).dao
    }

    @Provides
    fun provideApplicationSettingsRepository(
        dao: ApplicationSettingsDao
    ): ApplicationSettingsRepository {
        return ApplicationSettingsRepositoryImpl(dao)
    }
}