package com.schugarkub.dataguard.dependency.component

import com.schugarkub.dataguard.view.settings.SettingsFragment
import dagger.Component

@Component(dependencies = [ApplicationComponent::class])
interface FragmentComponent {

    fun inject(fragment: SettingsFragment)
}