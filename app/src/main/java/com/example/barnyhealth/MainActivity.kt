package com.example.barnyhealth

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private val chartsData = mutableMapOf<String, MutableList<Pair<Float, Float>>>()
    private val datesData = mutableMapOf<String, MutableList<Long>>()

    private lateinit var chart: LineChart
    private lateinit var spinnerParam: Spinner
    private lateinit var editValue: EditText
    private lateinit var btnDatePicker: Button
    private lateinit var btnAdd: Button
    private lateinit var btnDelete: Button
    private lateinit var btnShowList: Button
    private var currentParam: String = "WBC"

    private var selectedDate = Date()
    private lateinit var dataRepo: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chart = findViewById(R.id.chartWbc)
        spinnerParam = findViewById(R.id.spinnerParam)
        editValue = findViewById(R.id.editValue)
        btnDatePicker = findViewById(R.id.btnDatePicker)
        btnAdd = findViewById(R.id.btnAdd)
        btnDelete = findViewById(R.id.btnDelete)
        btnShowList = findViewById(R.id.btnShowList)

        dataRepo = DataRepository(this)
        chartsData.putAll(dataRepo.loadChartsData())
        datesData.putAll(dataRepo.loadDatesData())

        setupChart()
        setupSpinner()
        setupDatePicker()
        setupButton()
        setupDeleteButton()
        setupShowListButton()

        val firstParam = spinnerParam.selectedItem?.toString()
            ?: chartsData.keys.firstOrNull()
            ?: HealthParams.ALL_PARAMS.first()

        spinnerParam.setSelection(HealthParams.ALL_PARAMS.indexOf(firstParam).coerceAtLeast(0))
        updateChart(firstParam)
    }

    override fun onResume() {
        super.onResume()
        chartsData.clear()
        datesData.clear()
        chartsData.putAll(dataRepo.loadChartsData())
        datesData.putAll(dataRepo.loadDatesData())

        val param = spinnerParam.selectedItem?.toString() ?: HealthParams.ALL_PARAMS.first()
        updateChart(param)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        editValue.clearFocus()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && ev.action == MotionEvent.ACTION_DOWN) {
            if (currentFocus == editValue) hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
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

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setCenterAxisLabels(false)
        chart.xAxis.granularity = 1f

        chart.axisRight.isEnabled = false
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            HealthParams.ALL_PARAMS
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerParam.adapter = adapter

        spinnerParam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentParam = HealthParams.ALL_PARAMS[position]
                updateChart(currentParam)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButton() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        btnAdd.setOnClickListener {
            val param = spinnerParam.selectedItem.toString()
            val valueText = editValue.text.toString().trim()
            if (valueText.isEmpty()) return@setOnClickListener

            val rawValue = valueText.toFloatOrNull() ?: return@setOnClickListener

            val df = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US)).apply {
                roundingMode = RoundingMode.HALF_UP
            }
            val value = df.format(rawValue).toFloat()

            val newTimestamp = selectedDate.time

            val paramDates = datesData.getOrPut(param) { mutableListOf() }
            val paramValues = chartsData.getOrPut(param) { mutableListOf() }

            val existingIndex = paramDates.indexOfLast { it == newTimestamp }
            if (existingIndex >= 0) {
                paramValues[existingIndex] = Pair(paramValues[existingIndex].first, value)
                Toast.makeText(
                    this,
                    "📅 ${sdf.format(selectedDate)} обновлено: $value",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                paramDates.add(newTimestamp)
                paramValues.add(Pair(0f, value))

                dataRepo.saveChartsData(chartsData)
                dataRepo.saveDatesData(datesData)
            }

            updateChart(param)
            editValue.text.clear()
            hideKeyboard()
        }
    }

    private fun setupDeleteButton() {
        btnDelete.setOnClickListener {
            val param = spinnerParam.selectedItem.toString()

            chartsData.remove(param)
            datesData.remove(param)

            dataRepo.saveChartsData(chartsData)
            dataRepo.saveDatesData(datesData)

            updateChart(param)
            Toast.makeText(this, "🗑️ Удалены все данные: $param", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupShowListButton() {
        btnShowList.setOnClickListener {
            startActivity(Intent(this, HistoryListActivity::class.java))
        }
    }

    private fun updateChart(param: String) {
        val timestamps = datesData[param]?.toList() ?: emptyList()
        val values = chartsData[param]?.map { it.second } ?: emptyList()

        val pairedData = timestamps.zip(values) { timestamp, value ->
            Pair(timestamp, value)
        }.sortedBy { it.first }

        val sortedEntries = pairedData.mapIndexed { newIndex, pair ->
            Entry(newIndex.toFloat(), pair.second)
        }

        val sortedTimestamps = pairedData.map { it.first }

        val paramColor = HealthParams.COLORS[param] ?: Color.GRAY
        val norms = HealthParams.NORMS[param]

        setupYAxis(param, sortedEntries.map { it.y }, norms)

        if (sortedEntries.isEmpty()) {
            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = 4.5f
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("", "", "", "", ""))
        } else {
            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = (sortedEntries.size - 1 + 0.5f)
            val dateLabels = sortedTimestamps.map { timestamp ->
                SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
            chart.xAxis.labelCount = sortedEntries.size.coerceIn(3, 10)
        }

        chart.xAxis.setCenterAxisLabels(false)

        val mainDataSet = if (sortedEntries.isEmpty()) {
            LineDataSet(emptyList(), "Нет данных").apply {
                color = paramColor
                setDrawValues(false)
                lineWidth = 2f
                setDrawCircles(false)
            }
        } else {
            LineDataSet(sortedEntries, param).apply {
                color = paramColor
                setCircleColor(paramColor)
                lineWidth = 3f
                circleRadius = 6f
                setDrawCircleHole(false)
                setDrawValues(true)
                valueTextSize = 12f
                valueTextColor = Color.BLACK

                valueFormatter = object : ValueFormatter() {
                    private val df = DecimalFormat("0.0", DecimalFormatSymbols(Locale.US))
                    override fun getFormattedValue(value: Float): String = df.format(value)
                }
            }
        }

        val fillDataSet = createNormFillDataSet(param, sortedEntries.size, norms)

        val allDataSets = mutableListOf<ILineDataSet>()
        fillDataSet?.let { allDataSets.add(it) }
        allDataSets.add(mainDataSet)

        chart.data = LineData(allDataSets)

        setupNormLines(param, norms)

        chart.invalidate()
    }

    private fun setupYAxis(param: String, yValues: List<Float>, norms: Pair<Float, Float>?) {
        val dataMin = if (yValues.isEmpty()) 0f else yValues.minOf { it } * 0.9f
        val dataMax = if (yValues.isEmpty()) 0f else yValues.maxOf { it } * 1.2f

        norms?.let { range ->
            val fullMin = minOf(dataMin, range.first * 0.8f)
            val fullMax = maxOf(dataMax, range.second * 1.2f)
            chart.axisLeft.axisMinimum = maxOf(0f, fullMin)
            chart.axisLeft.axisMaximum = niceCeil(fullMax)
        } ?: run {
            chart.axisLeft.axisMinimum = maxOf(0f, dataMin)
            chart.axisLeft.axisMaximum = niceCeil(dataMax)
        }
    }

    private fun setupNormLines(param: String, norms: Pair<Float, Float>?) {
        chart.axisLeft.removeAllLimitLines()
        norms ?: return

        LimitLine(norms.first).apply {
            lineWidth = 2f
            lineColor = Color.GREEN
            label = ""
        }.also { chart.axisLeft.addLimitLine(it) }

        LimitLine(norms.second).apply {
            lineWidth = 2f
            lineColor = Color.GREEN
            enableDashedLine(10f, 5f, 0f)
            label = ""
        }.also { chart.axisLeft.addLimitLine(it) }
    }

    private fun createNormFillDataSet(param: String, pointCount: Int, norms: Pair<Float, Float>?): LineDataSet? {
        norms ?: return null

        val minX = -0.5f
        val maxX = if (pointCount == 0) 4.5f else (pointCount - 1 + 0.5f)

        val fillEntries = listOf(
            Entry(minX, norms.second),
            Entry(maxX, norms.second)
        )

        return LineDataSet(fillEntries, "Зона нормы").apply {
            setDrawFilled(true)
            fillColor = Color.rgb(220, 255, 220)
            fillAlpha = 150
            color = Color.TRANSPARENT
            lineWidth = 0f
            setDrawCircles(false)
            setDrawValues(false)
            isHighlightEnabled = false
            fillFormatter = IFillFormatter { _, _ -> norms.first }
        }
    }

    private fun niceCeil(value: Float): Float {
        if (value <= 0f) return 1f
        val log10Value = log10(value.toDouble())
        val floorLog = floor(log10Value)
        val pow10 = 10.0.pow(floorLog)
        val scaled = value / pow10.toFloat()
        val niceScaled = when {
            scaled <= 1.0f -> 1.0f
            scaled <= 2.0f -> 2.0f
            scaled <= 5.0f -> 5.0f
            else -> 10.0f
        }
        return (niceScaled * pow10).toFloat()
    }
}
