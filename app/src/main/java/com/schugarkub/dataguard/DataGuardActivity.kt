package com.schugarkub.dataguard

import android.app.AppOpsManager
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.provider.Settings
import androidx.core.app.AppOpsManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.schugarkub.dataguard.service.NetworkMonitoringService
import com.schugarkub.dataguard.view.applicationslist.ApplicationsListFragment
import com.schugarkub.dataguard.view.notificationsjournal.NotificationsJournalFragment
import com.schugarkub.dataguard.view.preferences.KEY_NETWORK_MONITORING_SERVICE_BINDER
import com.schugarkub.dataguard.view.preferences.PreferencesBottomSheetFragment
import timber.log.Timber

private const val REQUEST_USAGE_ACCESS = 100

class DataGuardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private var serviceBinder: NetworkMonitoringService.NetworkMonitoringBinder? = null
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragment()

        val serviceIntent = Intent(this, NetworkMonitoringService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, 0)

        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.applications_list_menu_item -> {
                        updateFragment(ApplicationsListFragment::class.java)
                        true
                    }
                    R.id.notifications_journal_menu_item -> {
                        updateFragment(NotificationsJournalFragment::class.java)
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

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_USAGE_ACCESS) {
            startNetworkMonitoring()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startNetworkMonitoring() {
        if (isUsageAccessAllowed) {
            serviceBinder?.let {
                if (!it.isNetworkMonitoringEnabled) {
                    it.startNetworkMonitoring()
                }
            }
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
                arguments = bundleOf(
                    KEY_NETWORK_MONITORING_SERVICE_BINDER to serviceBinder
                )
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

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("Connected to service")
            serviceBinder = service as NetworkMonitoringService.NetworkMonitoringBinder
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("Disconnected from service")
            serviceBinder = null
            isBound = false
        }
    }
}