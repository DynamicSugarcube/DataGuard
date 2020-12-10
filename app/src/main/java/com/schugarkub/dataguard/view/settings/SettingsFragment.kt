package com.schugarkub.dataguard.view.settings

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.schugarkub.dataguard.DataGuardApplication
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.utils.floatToPercent
import com.schugarkub.dataguard.viewmodel.SettingsViewModel
import com.schugarkub.dataguard.viewmodel.SettingsViewModelFactory
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var viewModelFactory: SettingsViewModelFactory
    private lateinit var viewModel: SettingsViewModel

    private var thresholdPreference: EditTextPreference? = null
    private var maxDeviationPreference: EditTextPreference? = null
    private var learningIterationsPreference: EditTextPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        thresholdPreference = findPreference(
            requireContext().getString(R.string.threshold_preference_key)
        )

        maxDeviationPreference = findPreference(
            requireContext().getString(R.string.deviation_preference_key)
        )

        learningIterationsPreference = findPreference(
            requireContext().getString(R.string.learning_iterations_preference_key)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as DataGuardApplication)
            .fragmentComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SettingsViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThresholdPreference()
        setupMaxBytesRateDeviationPreference()
        setupLearningIterationsPreference()
    }

    private fun setupThresholdPreference() {
        thresholdPreference?.let { preference ->
            viewModel.thresholdLiveData.observe(
                viewLifecycleOwner, { threshold ->
                    preference.text = threshold.toString()
                }
            )

            preference.setOnBindEditTextListener(
                PreferenceOnBindEditTextListener(MaxLength.THRESHOLD)
            )
            preference.setOnPreferenceChangeListener { _, newValue ->
                viewModel.onThresholdChangedCallback(newValue)
            }
        }
    }

    private fun setupMaxBytesRateDeviationPreference() {
        maxDeviationPreference?.let { preference ->
            viewModel.maxBytesRateDeviationLiveData.observe(
                viewLifecycleOwner, { maxDeviation ->
                    preference.text = floatToPercent(maxDeviation).toString()
                }
            )

            preference.setOnBindEditTextListener(
                PreferenceOnBindEditTextListener(MaxLength.DEVIATION)
            )
            preference.setOnPreferenceChangeListener { _, newValue ->
                viewModel.onMaxBytesRateDeviationChangedCallback(newValue)
            }
        }
    }

    private fun setupLearningIterationsPreference() {
        learningIterationsPreference?.let { preference ->
            viewModel.learningIterationsLiveData.observe(
                viewLifecycleOwner, { times ->
                    preference.text = times.toString()
                }
            )

            preference.setOnBindEditTextListener(
                PreferenceOnBindEditTextListener(MaxLength.CALIBRATION_TIMES)
            )
            preference.setOnPreferenceChangeListener { _, newValue ->
                viewModel.onLearningIterationsChangedCallback(newValue)
            }
        }
    }

    companion object {

        private enum class MaxLength(val value: Int) {
            THRESHOLD(Long.MAX_VALUE.toString().length),
            DEVIATION(2),
            CALIBRATION_TIMES(Int.MAX_VALUE.toString().length)
        }

        private class PreferenceOnBindEditTextListener(maxLength: MaxLength) :
            EditTextPreference.OnBindEditTextListener {

            private val inputType = InputType.TYPE_CLASS_NUMBER

            private val filters = arrayOf(
                InputFilter { source, _, _, dest, _, _ ->
                    if (source.toString() == "0" && dest.toString() == "0") "" else null
                },
                InputFilter.LengthFilter(maxLength.value)
            )

            override fun onBindEditText(editText: EditText) {
                editText.inputType = inputType
                editText.filters = filters

                editText.doAfterTextChanged {
                    if (it.isNullOrBlank()) {
                        editText.modifyText("0")
                        return@doAfterTextChanged
                    }
                    val originalText = it.toString()
                    try {
                        val numberText = originalText.toLong().toString()
                        if (originalText != numberText) {
                            editText.modifyText(numberText)
                        }
                    } catch (e: Exception) {
                        editText.modifyText("0")
                    }
                }
            }

            private fun EditText.modifyText(text: String) {
                setText(text)
                setSelection(text.length)
            }
        }
    }
}