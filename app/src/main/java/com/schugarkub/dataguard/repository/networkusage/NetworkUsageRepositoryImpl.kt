package com.schugarkub.dataguard.repository.networkusage

import com.schugarkub.dataguard.database.networkusage.NetworkUsageDao
import com.schugarkub.dataguard.model.NetworkUsageEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUsageRepositoryImpl @Inject constructor(
    private val dao: NetworkUsageDao
) : NetworkUsageRepository {

    override suspend fun addEntity(entity: NetworkUsageEntity) {
        dao.insert(entity)
    }

    override suspend fun updateEntity(entity: NetworkUsageEntity) {
        dao.update(entity)
    }

    override suspend fun getEntityByPackageName(packageName: String): NetworkUsageEntity? {
        return dao.getByPackageName(packageName)
    }

    override suspend fun deleteAllEntities() {
        dao.clean()
    }
}