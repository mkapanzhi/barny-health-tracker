package com.example.barnyhealth

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class BulkEntryActivity : AppCompatActivity() {

    private lateinit var btnDatePicker: Button
    private lateinit var radioGroupMode: RadioGroup
    private lateinit var rbAll: RadioButton
    private lateinit var rbFavorites: RadioButton
    private lateinit var tvEmptyFavorites: TextView
    private lateinit var containerFields: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    private lateinit var dataRepo: DataRepository

    private var selectedDate = Date()

    private val inputMap = mutableMapOf<String, EditText>()
    private val rowMap = mutableMapOf<String, View>()

    private var chartsData = mutableMapOf<String, MutableList<Pair<Float, Float>>>()
    private var datesData = mutableMapOf<String, MutableList<Long>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulk_entry)

        btnDatePicker = findViewById(R.id.btnDatePickerBulk)
        radioGroupMode = findViewById(R.id.radioGroupMode)
        rbAll = findViewById(R.id.rbAll)
        rbFavorites = findViewById(R.id.rbFavorites)
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites)
        containerFields = findViewById(R.id.containerFields)
        btnSave = findViewById(R.id.btnSaveBulk)

        dataRepo = DataRepository(this)
        chartsData.putAll(dataRepo.loadChartsData())
        datesData.putAll(dataRepo.loadDatesData())

        setupDatePicker()
        buildFields()
        setupModeSwitcher()
        setupSaveButton()

        rbAll.isChecked = true
        applyFilter()

        btnBack = findViewById(R.id.btnBackBulk)
        btnBack.setOnClickListener {
            finish()
        }

    }

    private fun setupDatePicker() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        btnDatePicker.text = "📅 ${sdf.format(selectedDate)}"

        btnDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = selectedDate }

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    btnDatePicker.text = "📅 ${sdf.format(selectedDate)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun buildFields() {
        containerFields.removeAllViews()
        inputMap.clear()
        rowMap.clear()

        val sortedParams = HealthParams.ALL_PARAMS.sorted()

        sortedParams.forEach { param ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.TOP
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(14)
                }
            }

            val leftBlock = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2.3f
                ).apply {
                    marginEnd = dp(12)
                }
            }

            val shortName = HealthParams.ABBREVIATIONS[param] ?: param
            val fullName = if (shortName != param) param else ""

            val tvShortName = TextView(this).apply {
                text = shortName
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor("#2C2C2C"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val tvFullName = TextView(this).apply {
                text = if (fullName.isNotBlank()) "($fullName)" else ""
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                visibility = if (fullName.isNotBlank()) View.VISIBLE else View.GONE
            }

            val etValue = EditText(this).apply {
                hint = "Значение"
                inputType = InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
                setSingleLine()
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            leftBlock.addView(tvShortName)
            leftBlock.addView(tvFullName)

            row.addView(leftBlock)
            row.addView(etValue)

            containerFields.addView(row)

            inputMap[param] = etValue
            rowMap[param] = row
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

            val timestamp = selectedDate.time
            var savedCount = 0

            inputMap.forEach { (param, editText) ->
                val valueText = editText.text.toString().trim()
                if (valueText.isEmpty()) return@forEach

                val rawValue = valueText.toFloatOrNull() ?: return@forEach
                val value = df.format(rawValue).toFloat()

                val paramDates = datesData.getOrPut(param) { mutableListOf() }
                val paramValues = chartsData.getOrPut(param) { mutableListOf() }

                val existingIndex = paramDates.indexOfLast { it == timestamp }
                if (existingIndex >= 0) {
                    paramValues[existingIndex] = Pair(paramValues[existingIndex].first, value)
                } else {
                    paramDates.add(timestamp)
                    paramValues.add(Pair(0f, value))
                }

                savedCount++
            }

            dataRepo.saveChartsData(chartsData)
            dataRepo.saveDatesData(datesData)

            if (savedCount > 0) {
                Toast.makeText(
                    this,
                    "Сохранено параметров: $savedCount",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Нет заполненных значений для сохранения",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
