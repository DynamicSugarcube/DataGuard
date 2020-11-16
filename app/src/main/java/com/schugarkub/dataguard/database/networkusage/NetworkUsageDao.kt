package com.schugarkub.dataguard.database.networkusage

import androidx.room.*
import com.schugarkub.dataguard.model.NetworkUsageEntity

@Dao
interface NetworkUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NetworkUsageEntity)

    @Update
    suspend fun update(entity: NetworkUsageEntity)

    @Query("SELECT * FROM $NETWORK_USAGE_DATABASE_NAME WHERE package_name = :packageName")
    suspend fun getByPackageName(packageName: String): NetworkUsageEntity?

    @Query("DELETE FROM $NETWORK_USAGE_DATABASE_NAME")
    suspend fun clean()
}