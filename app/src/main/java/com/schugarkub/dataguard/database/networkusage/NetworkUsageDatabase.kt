package com.schugarkub.dataguard.database.networkusage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.schugarkub.dataguard.model.NetworkUsageEntity

const val NETWORK_USAGE_DATABASE_NAME = "network_usage_table"

@Database(entities = [NetworkUsageEntity::class], version = 1, exportSchema = false)
abstract class NetworkUsageDatabase : RoomDatabase() {

    abstract val dao: NetworkUsageDao

    companion object {

        @Volatile
        private var INSTANCE: NetworkUsageDatabase? = null

        fun getInstance(context: Context): NetworkUsageDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        NetworkUsageDatabase::class.java,
                        NETWORK_USAGE_DATABASE_NAME
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}