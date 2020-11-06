package com.schugarkub.dataguard.view.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.schugarkub.dataguard.R

class PreferencesBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "PreferencesBottomSheetFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preferences_bottom_sheet, container, false)
    }
}