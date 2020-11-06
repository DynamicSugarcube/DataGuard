package com.schugarkub.dataguard.view.preferences

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.utils.ACTION_NOTIFICATIONS_DATABASE_CLEAN

class PreferencesBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "PreferencesBottomSheetFragment"
    }

    private lateinit var controlNetworkMonitoringView: TextView
    private lateinit var cleanUpNotificationsView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_preferences_bottom_sheet, container, false)

        controlNetworkMonitoringView = layout.findViewById(R.id.control_network_monitoring)
        cleanUpNotificationsView = layout.findViewById(R.id.clean_up_notifications)

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        controlNetworkMonitoringView.setOnClickListener {
            Toast.makeText(requireContext(), "Not yet supported", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        cleanUpNotificationsView.setOnClickListener {
            requireContext().sendBroadcast(Intent(ACTION_NOTIFICATIONS_DATABASE_CLEAN))
            dismiss()
        }
    }
}