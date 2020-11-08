package com.schugarkub.dataguard.view.preferences

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper.ACTION_CONTROL_NETWORK_MONITORING
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper.EXTRA_NETWORK_MONITORING_ENABLED
import com.schugarkub.dataguard.monitoring.NetworkMonitoringHelper.KEY_NETWORK_MONITORING_ENABLED
import com.schugarkub.dataguard.utils.ACTION_NOTIFICATIONS_DATABASE_CLEAN

class PreferencesBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "PreferencesBottomSheetFragment"
    }

    private lateinit var controlNetworkMonitoringView: TextView
    private lateinit var cleanUpNotificationsView: TextView

    private var networkMonitoringEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_preferences_bottom_sheet, container, false)

        controlNetworkMonitoringView = layout.findViewById(R.id.control_network_monitoring)
        networkMonitoringEnabled = arguments?.get(KEY_NETWORK_MONITORING_ENABLED) as Boolean
        if (networkMonitoringEnabled) {
            controlNetworkMonitoringView.apply {
                text = getString(R.string.disable_network_monitoring)
                setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search_off, 0, 0, 0)
            }
        } else {
            controlNetworkMonitoringView.apply {
                text = getString(R.string.enable_network_monitoring)
                setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0)
            }
        }

        cleanUpNotificationsView = layout.findViewById(R.id.clean_up_notifications)

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        controlNetworkMonitoringView.setOnClickListener {
            val intent = Intent().apply {
                action = ACTION_CONTROL_NETWORK_MONITORING
                putExtra(EXTRA_NETWORK_MONITORING_ENABLED, !networkMonitoringEnabled)
            }
            requireContext().sendBroadcast(intent)
            dismiss()
        }

        cleanUpNotificationsView.setOnClickListener {
            requireContext().sendBroadcast(Intent(ACTION_NOTIFICATIONS_DATABASE_CLEAN))
            dismiss()
        }
    }
}