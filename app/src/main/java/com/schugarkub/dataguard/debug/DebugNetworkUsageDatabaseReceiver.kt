package com.schugarkub.dataguard.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDao
import com.schugarkub.dataguard.database.networkusage.NetworkUsageDatabase
import kotlinx.coroutines.*

private const val ACTION_CLEAN_NETWORK_USAGE_DATABASE =
    "com.schugarkub.dataguard.action.CLEAN_NETWORK_USAGE_DATABASE"

class DebugNetworkUsageDatabaseReceiver : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var dao: NetworkUsageDao

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            dao = NetworkUsageDatabase.getInstance(context).dao
            if (intent.action == ACTION_CLEAN_NETWORK_USAGE_DATABASE) {
                coroutineScope.launch {
                    dao.clean()
                }
                Toast.makeText(context, R.string.reset_network_usage_string, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}