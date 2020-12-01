package com.schugarkub.dataguard.database.applicationsettings

import androidx.room.*
import com.schugarkub.dataguard.model.ApplicationSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationSettingsDao {

    @Query("SELECT * FROM $APPLICATION_SETTINGS_DATABASE_NAME WHERE id = 0 LIMIT 1")
    fun getSettingsFlow(): Flow<ApplicationSettings?>

    @Query("SELECT * FROM $APPLICATION_SETTINGS_DATABASE_NAME WHERE id = 0 LIMIT 1")
    suspend fun getSettings(): ApplicationSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: ApplicationSettings)

    @Update
    suspend fun update(settings: ApplicationSettings)
}