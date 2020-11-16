package com.schugarkub.dataguard.database.notifications

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.schugarkub.dataguard.model.NotificationInfo

const val NOTIFICATIONS_DATABASE_NAME = "notifications_tables"

@Database(entities = [NotificationInfo::class], version = 1, exportSchema = false)
abstract class NotificationsDatabase : RoomDatabase() {

    abstract val dao: NotificationsDao

    companion object {

        @Volatile
        private var INSTANCE: NotificationsDatabase? = null

        fun getInstance(context: Context): NotificationsDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        NotificationsDatabase::class.java,
                        NOTIFICATIONS_DATABASE_NAME
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}