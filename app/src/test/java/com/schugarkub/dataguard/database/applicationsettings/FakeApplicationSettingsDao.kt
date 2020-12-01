package com.schugarkub.dataguard.database.applicationsettings

import com.schugarkub.dataguard.model.ApplicationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeApplicationSettingsDao : ApplicationSettingsDao{

    private var settings: ApplicationSettings? = null

    override fun getSettingsFlow(): Flow<ApplicationSettings?> {
        return flow {
            emit(settings)
        }
    }

    override suspend fun getSettings(): ApplicationSettings? {
        return settings
    }

    override suspend fun insert(settings: ApplicationSettings) {
        this.settings = settings
    }

    override suspend fun update(settings: ApplicationSettings) {
        this.settings = settings
    }

}