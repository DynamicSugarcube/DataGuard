package com.schugarkub.dataguard.dependency.component

import com.schugarkub.dataguard.view.applicationslist.ApplicationsListFragment
import com.schugarkub.dataguard.view.notificationsjournal.NotificationsJournalFragment
import com.schugarkub.dataguard.view.preferences.PreferencesBottomSheetFragment
import com.schugarkub.dataguard.view.settings.SettingsFragment
import dagger.Component

@Component(dependencies = [ApplicationComponent::class])
interface FragmentComponent {

    fun inject(fragment: SettingsFragment)
    fun inject(fragment: NotificationsJournalFragment)
    fun inject(fragment: ApplicationsListFragment)
    fun inject(fragment: PreferencesBottomSheetFragment)
}