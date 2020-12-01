package com.schugarkub.dataguard.database.applicationsettings

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.schugarkub.dataguard.model.ApplicationSettings

const val APPLICATION_SETTINGS_DATABASE_NAME = "app_settings_table"

@Database(entities = [ApplicationSettings::class], version = 1, exportSchema = false)
abstract class ApplicationSettingsDatabase : RoomDatabase() {

    abstract val dao: ApplicationSettingsDao

    companion object {

        @Volatile
        private var INSTANCE: ApplicationSettingsDatabase? = null

        fun getInstance(context: Context): ApplicationSettingsDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        ApplicationSettingsDatabase::class.java,
                        APPLICATION_SETTINGS_DATABASE_NAME
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}