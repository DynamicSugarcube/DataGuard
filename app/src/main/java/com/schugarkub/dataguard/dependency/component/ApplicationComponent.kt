package com.schugarkub.dataguard.dependency.component

import android.app.Application
import com.schugarkub.dataguard.dependency.module.ApplicationModule
import com.schugarkub.dataguard.dependency.module.ApplicationSettingsModule
import com.schugarkub.dataguard.repository.applicationsettings.ApplicationSettingsRepository
import dagger.Component

@Component(modules = [ApplicationModule::class, ApplicationSettingsModule::class])
interface ApplicationComponent {

    fun getApplication(): Application

    fun getApplicationSettingsRepository(): ApplicationSettingsRepository
}