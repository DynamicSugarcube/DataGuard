package com.schugarkub.dataguard.repository.networkusage

import com.schugarkub.dataguard.model.NetworkUsageEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before

import org.junit.Test

class NetworkUsageRepositoryImplTest {

    private lateinit var repository: NetworkUsageRepository

    @Before
    fun setUp() {
        val dao = FakeNetworkUsageDao()
        repository = NetworkUsageRepositoryImpl(dao)
    }

    @Test
    fun insertOneEntity_returnInserted() = runBlocking {
        val insertEntity = NETWORK_USAGE_ENTITIES.first()
        repository.addEntity(insertEntity)

        val actual = repository.getEntityByPackageName(insertEntity.packageName)

        Assert.assertEquals(insertEntity, actual)
    }

    @Test
    fun insertManyEntities_returnInserted() = runBlocking {
        NETWORK_USAGE_ENTITIES.forEach {  entity ->
            repository.addEntity(entity)
        }

        val actual = NETWORK_USAGE_ENTITIES.map { entity ->
            repository.getEntityByPackageName(entity.packageName)
        }

        Assert.assertEquals(NETWORK_USAGE_ENTITIES, actual)
    }

    @Test
    fun updateEntity_returnUpdated() = runBlocking {
        NETWORK_USAGE_ENTITIES.forEach { entity ->
            repository.addEntity(entity)
        }

        val updateEntity = NETWORK_USAGE_ENTITIES.random().apply {
            averageRxBytesRate = 200L
            averageTxBytesRate = 100L
        }

        repository.updateEntity(updateEntity)

        val actual = repository.getEntityByPackageName(updateEntity.packageName)

        Assert.assertEquals(updateEntity, actual)
    }

    @Test
    fun deleteAllEntities_returnNulls() = runBlocking {
        NETWORK_USAGE_ENTITIES.forEach { entity ->
            repository.addEntity(entity)
        }

        repository.deleteAllEntities()

        val entities = NETWORK_USAGE_ENTITIES.map { entity ->
            repository.getEntityByPackageName(entity.packageName)
        }

        entities.forEach { entity ->
            Assert.assertEquals(null, entity)
        }
    }

    companion object {
        private const val NUM_OF_NETWORK_USAGE_ENTITIES = 5
        private val NETWORK_USAGE_ENTITIES = List(NUM_OF_NETWORK_USAGE_ENTITIES) { index ->
            NetworkUsageEntity(packageName = "package$index")
        }
    }
}