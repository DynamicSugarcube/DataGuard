package com.schugarkub.dataguard.view.preferences

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.service.NetworkMonitoringService
import com.schugarkub.dataguard.utils.ACTION_NOTIFICATIONS_DATABASE_CLEAN

const val KEY_NETWORK_MONITORING_SERVICE_BINDER =
    "com.schugarkub.dataguard.key.KEY_NETWORK_MONITORING_SERVICE_BINDER"

class PreferencesBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "PreferencesBottomSheetFragment"
    }

    private lateinit var controlNetworkMonitoringView: TextView
    private lateinit var cleanUpNotificationsView: TextView

    private var serviceBinder: NetworkMonitoringService.NetworkMonitoringBinder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_preferences_bottom_sheet, container, false)

        controlNetworkMonitoringView = layout.findViewById(R.id.control_network_monitoring)
        serviceBinder = arguments?.get(KEY_NETWORK_MONITORING_SERVICE_BINDER) as
                NetworkMonitoringService.NetworkMonitoringBinder
        serviceBinder?.let {
            if (it.isNetworkMonitoringEnabled) {
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
        }

        cleanUpNotificationsView = layout.findViewById(R.id.clean_up_notifications)

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        controlNetworkMonitoringView.setOnClickListener {
            serviceBinder?.let {
                if (it.isNetworkMonitoringEnabled) {
                    it.stopNetworkMonitoring()
                } else {
                    it.startNetworkMonitoring()
                }
            }
            dismiss()
        }

        cleanUpNotificationsView.setOnClickListener {
            requireContext().sendBroadcast(Intent(ACTION_NOTIFICATIONS_DATABASE_CLEAN))
            dismiss()
        }
    }
}