package com.example.barnyhealth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val chartsData = mutableMapOf<String, MutableList<Pair<Float, Float>>>()
    private val datesData = mutableMapOf<String, MutableList<Long>>()

    private lateinit var chart: LineChart
    private lateinit var spinnerParam: Spinner
    private lateinit var btnParamInfo: ImageButton
    private lateinit var rvMeasurements: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var measurementAdapter: MeasurementAdapter
    private lateinit var dataRepo: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecycler()
        loadData()
        setupChart()
        setupSpinner()
        setupInfoButton()
        setupFab()
        applySafeArea()

        val firstParam = HealthParams.ALL_PARAMS.firstOrNull() ?: return
        spinnerParam.setSelection(HealthParams.ALL_PARAMS.indexOf(firstParam).coerceAtLeast(0))
        updateChart(firstParam)
    }

    private fun applySafeArea() {
        val root = findViewById<ConstraintLayout>(R.id.main)
        val fab = findViewById<FloatingActionButton>(R.id.fabAdd)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val topInset = maxOf(systemBars.top, cutout.top)
            val bottomInset = maxOf(systemBars.bottom, cutout.bottom)

            view.updatePadding(
                top = topInset,
                bottom = 0
            )

            fab.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = bottomInset + dp(20)
                marginEnd = dp(20)
            }

            insets
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        val selectedParam = spinnerParam.selectedItem?.toString() ?: HealthParams.ALL_PARAMS.first()
        updateChart(selectedParam)
    }

    private fun initViews() {
        chart = findViewById(R.id.chartWbc)
        spinnerParam = findViewById(R.id.spinnerParam)
        btnParamInfo = findViewById(R.id.btnParamInfo)
        rvMeasurements = findViewById(R.id.rvMeasurements)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun setupRecycler() {
        measurementAdapter = MeasurementAdapter(
            items = emptyList(),
            onItemLongPress = { item ->
                measurementAdapter.showDeleteFor(item.timestamp)
            },
            onDeleteClick = { item ->
                confirmDelete(item)
            },
            onItemClick = {
                measurementAdapter.hideDeleteButtons()
            }
        )

        rvMeasurements.layoutManager = LinearLayoutManager(this)
        rvMeasurements.adapter = measurementAdapter
    }

    private fun loadData() {
        dataRepo = DataRepository(this)
        chartsData.clear()
        datesData.clear()
        chartsData.putAll(dataRepo.loadChartsData())
        datesData.putAll(dataRepo.loadDatesData())
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            startActivity(Intent(this, BulkEntryActivity::class.java))
        }
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        chart.legend.isEnabled = false

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setCenterAxisLabels(false)
        chart.xAxis.granularity = 1f
        chart.xAxis.gridColor = Color.TRANSPARENT
        chart.xAxis.yOffset = 0f

        chart.axisRight.isEnabled = false
        chart.axisLeft.gridColor = Color.parseColor("#D9E6E1")
        chart.axisLeft.spaceBottom = 20f

        chart.setExtraOffsets(0f, 0f, 0f, 20f)
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
                val selectedParam = HealthParams.ALL_PARAMS[position]
                measurementAdapter.hideDeleteButtons()
                updateChart(selectedParam)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupInfoButton() {
        btnParamInfo.setOnClickListener {
            val param = spinnerParam.selectedItem?.toString() ?: return@setOnClickListener

            val abbreviation = HealthParams.ABBREVIATIONS[param] ?: param
            val description = HealthParams.DESCRIPTIONS[param] ?: "Описание пока не добавлено."
            val normPair = HealthParams.NORMS[param]

            val normText = if (normPair != null) {
                "Норма: ${
                    String.format(
                        Locale.US,
                        "%.1f",
                        normPair.first
                    )
                }–${String.format(Locale.US, "%.1f", normPair.second)}"
            } else {
                "Норма неизвестна"
            }

            MaterialAlertDialogBuilder(this)
                .setTitle("$abbreviation • $param")
                .setMessage("$description\n\n$normText")
                .setPositiveButton("Понятно", null)
                .show()
        }
    }

    private fun updateChart(param: String) {
        val timestamps = datesData[param]?.toList() ?: emptyList()
        val values = chartsData[param]?.map { it.second } ?: emptyList()

        val pairedData = timestamps.zip(values) { timestamp, value ->
            Pair(timestamp, value)
        }.sortedBy { it.first }

        val sortedEntries = pairedData.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second)
        }

        val sortedTimestamps = pairedData.map { it.first }
        val paramColor = HealthParams.COLORS[param] ?: Color.GRAY
        val norms = HealthParams.NORMS[param]

        setupYAxis(sortedEntries.map { it.y }, norms)
        setupXAxis(sortedEntries.size, sortedTimestamps)

        val mainDataSet = createMainDataSet(sortedEntries, param, paramColor)
        val fillDataSet = createNormFillDataSet(sortedEntries.size, norms)

        val allDataSets = mutableListOf<ILineDataSet>()
        fillDataSet?.let { allDataSets.add(it) }
        allDataSets.add(mainDataSet)

        chart.data = LineData(allDataSets)
        setupNormLines(norms)
        chart.invalidate()

        updateMeasurementsList(param)
    }

    private fun setupXAxis(entryCount: Int, timestamps: List<Long>) {
        if (entryCount == 0) {
            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = 4.5f
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("", "", "", "", ""))
            return
        }

        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = entryCount - 1 + 0.5f

        val dateLabels = timestamps.map { timestamp ->
            SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
        }

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
        chart.xAxis.labelCount = entryCount.coerceIn(3, 10)
    }

    private fun createMainDataSet(
        entries: List<Entry>,
        param: String,
        paramColor: Int
    ): LineDataSet {
        if (entries.isEmpty()) {
            return LineDataSet(emptyList(), "Нет данных").apply {
                color = paramColor
                setDrawValues(false)
                lineWidth = 2f
                setDrawCircles(false)
            }
        }

        return LineDataSet(entries, param).apply {
            color = paramColor
            setCircleColor(paramColor)
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(false)
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.20f

            valueFormatter = object : ValueFormatter() {
                private val df = DecimalFormat("0.0", DecimalFormatSymbols(Locale.US))
                override fun getFormattedValue(value: Float): String = df.format(value)
            }
        }
    }

    private fun setupYAxis(yValues: List<Float>, norms: Pair<Float, Float>?) {
        val greenZoneMin = norms?.first ?: yValues.minOrNull() ?: 0f
        val greenZoneMax = norms?.second ?: yValues.maxOrNull() ?: 10f

        val maxPoint = yValues.maxOrNull() ?: greenZoneMax
        val minPoint = yValues.minOrNull() ?: greenZoneMin

        val targetMax = maxOf(maxPoint, greenZoneMax)
        val targetMin = minOf(minPoint, greenZoneMin)

        var delta = targetMax - targetMin
        if (delta == 0f) delta = 1f

        val padding = delta * 0.25f
        val rawYMin = targetMin - padding
        val rawYMax = targetMax + padding

        val yMin: Float
        val yMax: Float

        if (rawYMin < 0f) {
            val cutAmount = 0f - rawYMin
            yMin = 0f
            yMax = rawYMax - cutAmount
        } else {
            yMin = rawYMin
            yMax = rawYMax
        }

        chart.axisLeft.axisMinimum = yMin
        chart.axisLeft.axisMaximum = yMax
    }

    private fun setupNormLines(norms: Pair<Float, Float>?) {
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

    private fun createNormFillDataSet(
        pointCount: Int,
        norms: Pair<Float, Float>?
    ): LineDataSet? {
        norms ?: return null

        val minX = -0.5f
        val maxX = if (pointCount == 0) 4.5f else (pointCount - 1 + 0.5f)

        val fillEntries = listOf(
            Entry(minX, norms.second),
            Entry(maxX, norms.second)
        )

        return LineDataSet(fillEntries, "").apply {
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

    private fun updateMeasurementsList(param: String) {
        val timestamps = datesData[param]?.toList() ?: emptyList()
        val values = chartsData[param]?.map { it.second } ?: emptyList()
        val norms = HealthParams.NORMS[param]
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val items = timestamps.zip(values) { timestamp, value ->
            Pair(timestamp, value)
        }
            .sortedByDescending { it.first }
            .map { (timestamp, value) ->
                val isOutOfNorm = norms?.let { value < it.first || value > it.second } ?: false

                MeasurementItem(
                    timestamp = timestamp,
                    date = dateFormat.format(Date(timestamp)),
                    paramName = HealthParams.ABBREVIATIONS[param] ?: param,
                    valueText = String.format(Locale.US, "%.1f", value),
                    isOutOfNorm = isOutOfNorm,
                    showDelete = false
                )
            }

        measurementAdapter.updateItems(items)
    }

    private fun confirmDelete(item: MeasurementItem) {
        val param = spinnerParam.selectedItem?.toString() ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle("Удалить запись?")
            .setMessage("${item.date} — ${item.valueText}")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                deleteMeasurement(param, item.timestamp)
            }
            .show()
    }

    private fun deleteMeasurement(param: String, timestamp: Long) {
        val paramDates = datesData[param] ?: return
        val paramValues = chartsData[param] ?: return

        val index = paramDates.indexOfFirst { it == timestamp }
        if (index == -1) return

        paramDates.removeAt(index)
        if (index < paramValues.size) {
            paramValues.removeAt(index)
        }

        if (paramDates.isEmpty()) {
            datesData.remove(param)
        }
        if (paramValues.isEmpty()) {
            chartsData.remove(param)
        }

        dataRepo.saveDatesData(datesData)
        dataRepo.saveChartsData(chartsData)

        measurementAdapter.hideDeleteButtons()
        updateChart(param)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}