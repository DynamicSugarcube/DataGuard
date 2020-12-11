package com.schugarkub.dataguard.dependency.module

import android.app.usage.NetworkStatsManager
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetriever
import com.schugarkub.dataguard.helpers.networkusage.NetworkUsageRetrieverImpl
import dagger.Module
import dagger.Provides

@Module
class NetworkUsageModule {

    @Provides
    fun provideNetworkUsageRetriever(manager: NetworkStatsManager): NetworkUsageRetriever {
        return NetworkUsageRetrieverImpl(manager)
    }
}