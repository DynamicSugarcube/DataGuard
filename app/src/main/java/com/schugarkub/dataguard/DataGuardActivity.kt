package com.schugarkub.dataguard

import android.app.AppOpsManager
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.core.app.AppOpsManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.schugarkub.dataguard.service.NetworkMonitoringService
import com.schugarkub.dataguard.view.applicationslist.ApplicationsListFragment
import com.schugarkub.dataguard.view.notificationsjournal.NotificationsJournalFragment
import com.schugarkub.dataguard.view.preferences.PreferencesBottomSheetFragment

private const val REQUEST_USAGE_ACCESS = 100

class DataGuardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragment()

        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.applications_toolbar_title)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.toolbar_search_menu_item -> {
                        // TODO Implement search
                        false
                    }
                    R.id.toolbar_preferences_menu_item -> {
                        showPreferences()
                        false
                    }
                    else -> false
                }
            }
        }

        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.applications_list_menu_item -> {
                        updateFragment(ApplicationsListFragment::class.java)
                        toolbar.setTitle(R.string.applications_toolbar_title)
                        true
                    }
                    R.id.notifications_journal_menu_item -> {
                        updateFragment(NotificationsJournalFragment::class.java)
                        toolbar.setTitle(R.string.notifications_toolbar_title)
                        true
                    }
                    R.id.preferences_menu_item -> {
                        showPreferences()
                        false
                    }
                    else -> false
                }
            }
        }

        // TODO Check if notifications are shown on top

        startNetworkMonitoring()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_USAGE_ACCESS) {
            startNetworkMonitoring()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startNetworkMonitoring() {
        if (isUsageAccessAllowed) {
            val serviceIntent = Intent(this, NetworkMonitoringService::class.java)
            startService(serviceIntent)
        }
    }

    private fun initFragment() {
        var fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null) {
            fragment = ApplicationsListFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    private fun <T : Fragment> updateFragment(fragmentClass: Class<T>) {
        var fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (!fragmentClass.isInstance(fragment)) {
            fragment = fragmentClass.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showPreferences() {
        supportFragmentManager.let {
            PreferencesBottomSheetFragment().apply {
                show(it, PreferencesBottomSheetFragment.TAG)
            }
        }
    }

    private val isUsageAccessAllowed: Boolean
        get() {
            val mode = AppOpsManagerCompat.noteOp(
                applicationContext,
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
            return if (mode == AppOpsManagerCompat.MODE_ALLOWED) {
                true
            } else {
                // TODO Show dialog describing why it's necessary
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivityForResult(intent, REQUEST_USAGE_ACCESS)
                false
            }
        }
}