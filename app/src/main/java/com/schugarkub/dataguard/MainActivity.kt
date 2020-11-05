package com.schugarkub.dataguard

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.core.app.AppOpsManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.schugarkub.dataguard.utils.ACTION_NOTIFICATION_SENT
import com.schugarkub.dataguard.utils.NotificationSentReceiver
import com.schugarkub.dataguard.view.applicationslist.ApplicationsListFragment

private const val REQUEST_USAGE_ACCESS = 100

private const val NETWORK_MONITOR_WORKER_NAME = "com.schugarkub.dataguard.NetworkMonitor"

class MainActivity : AppCompatActivity() {

    private val notificationSentBroadcastReceiver = NotificationSentReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createThresholdReachedNotificationChannel()

        registerReceiver(
            notificationSentBroadcastReceiver,
            IntentFilter(ACTION_NOTIFICATION_SENT)
        )

        // TODO Check if notifications are shown on top

        if (checkIfHaveUsageAccess()) {
            runNetworkMonitorWorker()
        }

        val containerId = R.id.fragment_container
        var fragment = supportFragmentManager.findFragmentById(containerId)
        if (fragment == null) {
            fragment = ApplicationsListFragment()
            supportFragmentManager.beginTransaction()
                .add(containerId, fragment)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationSentBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_USAGE_ACCESS) {
            if (checkIfHaveUsageAccess()) {
                runNetworkMonitorWorker()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun runNetworkMonitorWorker() {
        val monitorNetworkRequest = OneTimeWorkRequestBuilder<NetworkMonitorWorker>().build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                NETWORK_MONITOR_WORKER_NAME,
                ExistingWorkPolicy.KEEP,
                monitorNetworkRequest
            )
    }

    private fun createThresholdReachedNotificationChannel() {
        val channelId = getString(R.string.threshold_reached_notification_channel_id)
        val channelName = getString(R.string.threshold_reached_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun checkIfHaveUsageAccess(): Boolean {
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