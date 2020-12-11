package com.schugarkub.dataguard.repository.networkusage

import com.schugarkub.dataguard.model.NetworkUsageEntity

interface NetworkUsageRepository {

    suspend fun addEntity(entity: NetworkUsageEntity)

    suspend fun updateEntity(entity: NetworkUsageEntity)

    suspend fun getEntityByPackageName(packageName: String): NetworkUsageEntity?

    suspend fun deleteAllEntities()
}