package com.schugarkub.dataguard

import android.app.Application
import com.schugarkub.dataguard.dependency.component.*
import com.schugarkub.dataguard.dependency.module.ApplicationModule
import com.schugarkub.dataguard.dependency.module.ApplicationSettingsModule
import timber.log.Timber

@Suppress("unused")
class DataGuardApplication : Application() {

    private val applicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
        .applicationSettingsModule(ApplicationSettingsModule())
        .build()

    val fragmentComponent: FragmentComponent = DaggerFragmentComponent.builder()
        .applicationComponent(applicationComponent)
        .build()

    val serviceComponent: ServiceComponent = DaggerServiceComponent.builder()
        .applicationComponent(applicationComponent)
        .build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}