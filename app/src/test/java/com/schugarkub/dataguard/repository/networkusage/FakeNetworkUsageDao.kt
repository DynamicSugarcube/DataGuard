package com.schugarkub.dataguard.repository.networkusage

import com.schugarkub.dataguard.database.networkusage.NetworkUsageDao
import com.schugarkub.dataguard.model.NetworkUsageEntity

class FakeNetworkUsageDao : NetworkUsageDao {

    private val database = mutableListOf<NetworkUsageEntity>()

    override suspend fun insert(entity: NetworkUsageEntity) {
        database.add(entity)
    }

    override suspend fun update(entity: NetworkUsageEntity) {
        val index = database.indexOfFirst {
            it.packageName == entity.packageName
        }
        if (index >= 0) {
            database[index] = entity
        }
    }

    override suspend fun getByPackageName(packageName: String): NetworkUsageEntity? {
        return database.find { it.packageName == packageName }
    }

    override suspend fun clean() {
        database.clear()
    }
}