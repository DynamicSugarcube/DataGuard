package com.schugarkub.dataguard

import android.app.Application
import timber.log.Timber

@Suppress("unused")
class DataGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}