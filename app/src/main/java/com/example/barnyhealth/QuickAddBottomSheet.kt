package com.example.barnyhealth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuickAddBottomSheet : BottomSheetDialogFragment() {

    private lateinit var dataRepo: DataRepository

    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var selectedDateText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataRepo = DataRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_quick_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val param = requireArguments().getString(ARG_PARAM).orEmpty()

        val tvQuickParamShort = view.findViewById<TextView>(R.id.tvQuickParamShort)
        val tvQuickParamFull = view.findViewById<TextView>(R.id.tvQuickParamFull)
        val tvQuickDate = view.findViewById<TextView>(R.id.tvQuickDate)
        val tvQuickNorm = view.findViewById<TextView>(R.id.tvQuickNorm)
        val etQuickValue = view.findViewById<EditText>(R.id.etQuickValue)
        etQuickValue.filters = arrayOf(DecimalDigitsInputFilter(1))
        val tilQuickValue = view.findViewById<TextInputLayout>(R.id.tilQuickValue)
        val btnQuickCancel = view.findViewById<MaterialButton>(R.id.btnQuickCancel)
        val btnQuickSave = view.findViewById<MaterialButton>(R.id.btnQuickSave)

        val shortName = HealthParams.ABBREVIATIONS[param] ?: param
        val fullName = param
        val normPair = HealthParams.NORMS[param]

        selectedDateText = formatDate(selectedTimestamp)

        tvQuickParamShort.text = shortName
        tvQuickParamFull.text = fullName
        tvQuickDate.text = selectedDateText
        tvQuickNorm.text = if (normPair != null) {
            "Норма: ${String.format(Locale.US, "%.1f", normPair.first)}–${
                String.format(Locale.US, "%.1f", normPair.second)
            }"
        } else {
            "Норма неизвестна"
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

            val roundedValue = kotlin.math.round(parsedValue * 10f) / 10f

            saveMeasurement(param, selectedTimestamp, roundedValue)

            tilQuickValue.error = null

            saveMeasurement(param, selectedTimestamp, parsedValue)

            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_PARAM to param)
            )

            Toast.makeText(requireContext(), "Значение добавлено", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun showDatePicker(tvQuickDate: TextView) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(selectedTimestamp)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedTimestamp = selection
            selectedDateText = formatDate(selection)
            tvQuickDate.text = selectedDateText
        }

        picker.show(parentFragmentManager, "quick_add_date_picker")
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    private fun saveMeasurement(param: String, timestamp: Long, value: Float) {
        val chartsData = dataRepo.loadChartsData().toMutableMap()
        val datesData = dataRepo.loadDatesData().toMutableMap()

        val valuesList = chartsData.getOrPut(param) { mutableListOf() }
        val datesList = datesData.getOrPut(param) { mutableListOf() }

        val existingIndex = datesList.indexOfFirst { existingTimestamp ->
            formatDate(existingTimestamp) == formatDate(timestamp)
        }

        if (existingIndex != -1) {
            datesList[existingIndex] = timestamp
            valuesList[existingIndex] = Pair(valuesList[existingIndex].first, value)
        } else {
            valuesList.add(Pair(valuesList.size.toFloat(), value))
            datesList.add(timestamp)
        }

        dataRepo.saveChartsData(chartsData)
        dataRepo.saveDatesData(datesData)
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