package com.schugarkub.dataguard

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.core.app.AppOpsManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper.ACTION_CONTROL_NETWORK_MONITORING
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper.EXTRA_NETWORK_MONITORING_ENABLED
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper.KEY_NETWORK_MONITORING_ENABLED
import com.schugarkub.dataguard.utils.ACTION_NOTIFICATIONS_DATABASE_CLEAN
import com.schugarkub.dataguard.utils.ACTION_NOTIFICATION_SENT
import com.schugarkub.dataguard.utils.NotificationsDatabaseInteractionReceiver
import com.schugarkub.dataguard.utils.NotificationsHelper
import com.schugarkub.dataguard.view.applicationslist.ApplicationsListFragment
import com.schugarkub.dataguard.view.notificationsjournal.NotificationsJournalFragment
import com.schugarkub.dataguard.view.preferences.PreferencesBottomSheetFragment
import timber.log.Timber
import java.util.*

private const val REQUEST_USAGE_ACCESS = 100

class DataGuardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private val notificationSentBroadcastReceiver = NotificationsDatabaseInteractionReceiver()

    private val networkMonitoringWorkControlReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && context != null) {
                if (intent.action == ACTION_CONTROL_NETWORK_MONITORING) {
                    if (intent.getBooleanExtra(EXTRA_NETWORK_MONITORING_ENABLED, false)) {
                        Timber.d("Enable network monitoring")
                        scheduleNetworkMonitoringWork()
                    } else {
                        Timber.d("Disable network monitoring")
                        NetworkMonitoringHelper.cancelWork(context)
                    }
                }
            }
        }
    }

    private var networkMonitoringWorkId: UUID? = null
    private var networkMonitoringEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragment()

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
                        true
                    }
                    else -> false
                }
            }
        }

        NotificationsHelper.createNotificationChannels(applicationContext)

        registerReceiver(
            notificationSentBroadcastReceiver,
            IntentFilter().apply {
                addAction(ACTION_NOTIFICATION_SENT)
                addAction(ACTION_NOTIFICATIONS_DATABASE_CLEAN)
            }
        )

        registerReceiver(
            networkMonitoringWorkControlReceiver,
            IntentFilter(ACTION_CONTROL_NETWORK_MONITORING)
        )

        // TODO Check if notifications are shown on top

        scheduleNetworkMonitoringWork()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationSentBroadcastReceiver)
        unregisterReceiver(networkMonitoringWorkControlReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_USAGE_ACCESS) {
            scheduleNetworkMonitoringWork()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun scheduleNetworkMonitoringWork() {
        if (haveUsageAccess()) {
            networkMonitoringWorkId = NetworkMonitoringHelper.scheduleWork(this)

            networkMonitoringWorkId?.let { uuid ->
                WorkManager
                    .getInstance(applicationContext)
                    .getWorkInfoByIdLiveData(uuid)
                    .observe(this) { info ->
                        networkMonitoringEnabled =
                            info != null && info.state == WorkInfo.State.RUNNING
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
                    KEY_NETWORK_MONITORING_ENABLED to networkMonitoringEnabled
                )
                show(it, PreferencesBottomSheetFragment.TAG)
            }
        }
    }

    private fun haveUsageAccess(): Boolean {
        val mode = AppOpsManagerCompat.noteOp(
            applicationContext, AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
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