package com.schugarkub.dataguard.dependency.component

import com.schugarkub.dataguard.service.NetworkMonitoringService
import dagger.Component

@Component(dependencies = [ApplicationComponent::class])
interface ServiceComponent {

    fun inject(service: NetworkMonitoringService)
}