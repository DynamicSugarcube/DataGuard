package com.schugarkub.dataguard.dependency.module

import android.app.Application
import android.app.usage.NetworkStatsManager
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDao
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDatabase
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetriever
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetrieverImpl
import com.schugarkub.dataguard.repository.networkusage.NetworkUsageRepository
import com.schugarkub.dataguard.repository.networkusage.NetworkUsageRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class NetworkUsageModule {

    @Provides
    fun provideNetworkUsageRetriever(manager: NetworkStatsManager): NetworkUsageRetriever {
        return NetworkUsageRetrieverImpl(manager)
    }

    @Provides
    fun provideNetworkUsageDao(application: Application): NetworkUsageDao {
        return NetworkUsageDatabase.getInstance(application.applicationContext).dao
    }

    @Provides
    fun provideNetworkUsageRepository(dao: NetworkUsageDao): NetworkUsageRepository {
        return NetworkUsageRepositoryImpl(dao)
    }
}