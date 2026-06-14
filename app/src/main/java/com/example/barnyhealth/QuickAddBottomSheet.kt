package com.example.barnyhealth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.barnyhealth.app.App
import com.example.barnyhealth.domain.model.MetricSource
import com.example.barnyhealth.domain.model.MetricUiModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

class QuickAddBottomSheet : BottomSheetDialogFragment() {



    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var selectedDateText: String = ""
    private var currentMetricModel: MetricUiModel? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_quick_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paramKey = requireArguments().getString(ARG_PARAM).orEmpty()

        val tvQuickParamShort = view.findViewById<TextView>(R.id.tvQuickParamShort)
        val tvQuickParamFull = view.findViewById<TextView>(R.id.tvQuickParamFull)
        val tvQuickDate = view.findViewById<TextView>(R.id.tvQuickDate)
        val tvQuickNorm = view.findViewById<TextView>(R.id.tvQuickNorm)
        val etQuickValue = view.findViewById<EditText>(R.id.etQuickValue)
        val tilQuickValue = view.findViewById<TextInputLayout>(R.id.tilQuickValue)
        val btnQuickCancel = view.findViewById<MaterialButton>(R.id.btnQuickCancel)
        val btnQuickSave = view.findViewById<MaterialButton>(R.id.btnQuickSave)

        etQuickValue.filters = arrayOf(DecimalDigitsInputFilter(1))

        selectedDateText = formatDate(selectedTimestamp)
        tvQuickDate.text = selectedDateText

        viewLifecycleOwner.lifecycleScope.launch {
            val app = requireActivity().application as App

            currentMetricModel = try {
                app.appContainer.getHomeMetricsUseCase()
                    .firstOrNull { it.key == paramKey }
            } catch (_: Throwable) {
                null
            }

            val model = currentMetricModel

            tvQuickParamShort.text = model?.abbreviation ?: paramKey
            tvQuickParamFull.text = model?.displayName ?: paramKey
            tvQuickNorm.text = formatNormText(model)
        }

        tvQuickDate.setOnClickListener {
            showDatePicker(tvQuickDate)
        }

        btnQuickCancel.setOnClickListener {
            dismiss()
        }

        btnQuickSave.setOnClickListener {
            val rawValue = etQuickValue.text?.toString()?.trim().orEmpty()

            if (rawValue.isBlank()) {
                tilQuickValue.error = "Введите значение"
                return@setOnClickListener
            }

            val parsedValue = rawValue.replace(",", ".").toFloatOrNull()
            if (parsedValue == null) {
                tilQuickValue.error = "Введите число"
                return@setOnClickListener
            }

            tilQuickValue.error = null

            val roundedValue = round(parsedValue * 10f) / 10f

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    saveMeasurement(
                        paramKey = paramKey,
                        value = roundedValue
                    )

                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(RESULT_PARAM to paramKey)
                    )

                    Toast.makeText(
                        requireContext(),
                        "Значение добавлено",
                        Toast.LENGTH_SHORT
                    ).show()

                    dismiss()
                } catch (t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка сохранения: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun saveMeasurement(paramKey: String, value: Float) {
        val app = requireActivity().application as App

        app.appContainer.saveMetricMeasurementUseCase(
            model = currentMetricModel,
            fallbackParamKey = paramKey,
            value = value,
            measuredAt = selectedTimestamp,
            source = "quick_add"
        )
    }

    private fun formatNormText(model: MetricUiModel?): String {
        val normMin = model?.normMin
        val normMax = model?.normMax

        return if (normMin != null && normMax != null) {
            val unitSuffix = model.unit.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
            "Норма: ${String.format(Locale.US, "%.1f", normMin)}–${
                String.format(Locale.US, "%.1f", normMax)
            }$unitSuffix"
        } else {
            "Норма неизвестна"
        }
    }

    private fun showDatePicker(tvQuickDate: TextView) {
        val tag = "quick_add_date_picker"
        val fm = parentFragmentManager

        if (fm.findFragmentByTag(tag) != null) {
            return
        }

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(selectedTimestamp)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedTimestamp = selection
            selectedDateText = formatDate(selection)
            tvQuickDate.text = selectedDateText
        }

        picker.show(fm, tag)
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    companion object {
        const val REQUEST_KEY = "quick_add_result"
        const val RESULT_PARAM = "result_param"
        private const val ARG_PARAM = "arg_param"

        fun newInstance(param: String): QuickAddBottomSheet {
            return QuickAddBottomSheet().apply {
                arguments = bundleOf(ARG_PARAM to param)
            }
        }
    }
}