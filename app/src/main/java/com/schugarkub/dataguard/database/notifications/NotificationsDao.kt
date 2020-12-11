package com.schugarkub.dataguard.database.notifications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.schugarkub.dataguard.model.NotificationInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationInfo): Long

    @Query("SELECT * FROM $NOTIFICATIONS_DATABASE_NAME ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<NotificationInfo>?>

    @Query("DELETE FROM $NOTIFICATIONS_DATABASE_NAME")
    suspend fun clean()
}