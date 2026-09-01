package com.example.barnyhealth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barnyhealth.app.App
import com.example.barnyhealth.domain.model.MetricUiModel
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Log

class BulkEntryFragment : Fragment() {

    companion object {
        private const val TAG = "BulkEntry"
    }

    private lateinit var btnDatePicker: Button
    private lateinit var radioGroupMode: RadioGroup
    private lateinit var rbAll: RadioButton
    private lateinit var rbFavorites: RadioButton
    private lateinit var tvEmptyFavorites: TextView
    private lateinit var containerFields: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    private var selectedDate = Date()

    private val inputMap = mutableMapOf<String, EditText>()
    private val rowMap = mutableMapOf<String, View>()

    private var metricModels: List<MetricUiModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(TAG, "onCreateView ${hashCode()}")
        return inflater.inflate(R.layout.activity_bulk_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "onViewCreated ${hashCode()}")

        btnDatePicker = view.findViewById(R.id.btnDatePickerBulk)
        radioGroupMode = view.findViewById(R.id.radioGroupMode)
        rbAll = view.findViewById(R.id.rbAll)
        rbFavorites = view.findViewById(R.id.rbFavorites)
        tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites)
        containerFields = view.findViewById(R.id.containerFields)
        btnSave = view.findViewById(R.id.btnSaveBulk)
        btnBack = view.findViewById(R.id.btnBackBulk)

        selectedDate = normalizeDate(selectedDate)

        setupDatePicker()
        setupModeSwitcher()
        setupSaveButton()

        viewLifecycleOwner.lifecycleScope.launch {
            Log.e(TAG, "loading metricModels ${hashCode()}")

            val app = requireActivity().application as App

            metricModels = try {
                app.appContainer.getHomeMetricsUseCase()
                    .sortedBy { it.displayName.lowercase(Locale.getDefault()) }
            } catch (t: Throwable) {
                Log.e(TAG, "metric load error", t)
                emptyList()
            }

            Log.e(TAG, "metricModels loaded count=${metricModels.size} ${hashCode()}")

            buildFields()
            rbAll.isChecked = true
            applyFilter()
        }

        btnBack.setOnClickListener {
            Log.e(TAG, "back click ${hashCode()}")
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupDatePicker() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        btnDatePicker.text = "📅 ${sdf.format(selectedDate)}"
        Log.e(TAG, "setupDatePicker ${hashCode()}")

        btnDatePicker.setOnClickListener {
            Log.e(TAG, "date click ${hashCode()}")

            val tag = "bulk_date_picker"
            val fm = childFragmentManager

            val existing = fm.findFragmentByTag(tag)
            Log.e(TAG, "existing dialog = $existing")

            if (existing != null) {
                Log.e(TAG, "dialog already exists ${hashCode()}")
                return@setOnClickListener
            }

            Log.e(TAG, "before show ${hashCode()}")

            DatePickerFragment(
                initialTimeMillis = selectedDate.time
            ) { year, month, day ->
                Log.e(TAG, "date selected y=$year m=$month d=$day ${hashCode()}")

                val calendar = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                selectedDate = normalizeDate(calendar.time)
                btnDatePicker.text = "📅 ${sdf.format(selectedDate)}"
            }.show(fm, tag)

            Log.e(TAG, "after show ${hashCode()}")
        }
    }

    private fun buildFields() {
        containerFields.removeAllViews()
        inputMap.clear()
        rowMap.clear()
        val onSurfaceColor = resolveColor(R.color.md_theme_onSurface)
        val onSurfaceVariantColor = resolveColor(R.color.md_theme_onSurfaceVariant)
        val outlineColor = resolveColor(R.color.md_theme_outline)

        metricModels.forEach { model ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.TOP
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(14)
                }
            }

            val leftBlock = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.8f
                ).apply {
                    marginEnd = dp(12)
                }
            }

            val shortName = model.abbreviation.ifBlank { model.displayName }
            val fullName = model.displayName.takeIf { it != shortName }.orEmpty()

            val tvShortName = TextView(requireContext()).apply {
                text = shortName
                textSize = 18f
                setTextColor(onSurfaceColor)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val tvFullName = TextView(requireContext()).apply {
                text = if (fullName.isNotBlank()) "($fullName)" else ""
                textSize = 14f
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                setTextColor(onSurfaceVariantColor)
                visibility = if (fullName.isNotBlank()) View.VISIBLE else View.GONE
            }

            val etValue = EditText(requireContext()).apply {
                hint = getString(R.string.add)
                inputType = InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
                setSingleLine()

                setTextColor(onSurfaceColor)
                setHintTextColor(onSurfaceVariantColor)
                backgroundTintList = android.content.res.ColorStateList.valueOf(outlineColor)

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.2f
                )
            }

            leftBlock.addView(tvShortName)
            leftBlock.addView(tvFullName)

            row.addView(leftBlock)
            row.addView(etValue)

            containerFields.addView(row)

            inputMap[model.key] = etValue
            rowMap[model.key] = row
        }
    }

    private fun setupModeSwitcher() {
        radioGroupMode.setOnCheckedChangeListener { _, _ ->
            applyFilter()
        }
    }

    private fun applyFilter() {
        val showFavoritesOnly = rbFavorites.isChecked

        if (!showFavoritesOnly) {
            rowMap.values.forEach { it.visibility = View.VISIBLE }
            tvEmptyFavorites.visibility = View.GONE
            containerFields.visibility = View.VISIBLE
            return
        }

        var visibleCount = 0

        inputMap.forEach { (param, editText) ->
            val hasValue = editText.text.toString().trim().isNotEmpty()
            rowMap[param]?.visibility = if (hasValue) {
                visibleCount++
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        if (visibleCount == 0) {
            tvEmptyFavorites.visibility = View.VISIBLE
            containerFields.visibility = View.GONE
        } else {
            tvEmptyFavorites.visibility = View.GONE
            containerFields.visibility = View.VISIBLE
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val df = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US)).apply {
                roundingMode = RoundingMode.HALF_UP
            }

            val normalizedTimestamp = normalizeDate(selectedDate).time

            viewLifecycleOwner.lifecycleScope.launch {
                val app = requireActivity().application as App
                var savedCount = 0

                inputMap.forEach { (param, editText) ->
                    val valueText = editText.text.toString().trim()
                    if (valueText.isEmpty()) return@forEach

                    val rawValue = valueText.toFloatOrNull() ?: return@forEach
                    val value = df.format(rawValue).toFloat()

                    val model = metricModels.firstOrNull { it.key == param }

                    app.appContainer.saveMetricMeasurementUseCase(
                        model = model,
                        fallbackParamKey = param,
                        value = value,
                        measuredAt = normalizedTimestamp,
                        source = "bulk_entry"
                    )

                    savedCount++
                }

                if (savedCount > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Сохранено параметров: $savedCount",
                        Toast.LENGTH_LONG
                    ).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Нет заполненных значений для сохранения",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun normalizeDate(date: Date): Date {
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun normalizeTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
    private fun resolveColor(colorRes: Int): Int {
        return ContextCompat.getColor(requireContext(), colorRes)
    }

}