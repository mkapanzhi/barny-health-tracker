package com.example.barnyhealth

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barnyhealth.app.App
import com.example.barnyhealth.data.local.db.entity.MeasurementEntity
import com.example.barnyhealth.domain.model.MetricSource
import com.example.barnyhealth.domain.model.MetricUiModel
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private val chartsData = mutableMapOf<String, MutableList<Pair<Float, Float>>>()
    private val datesData = mutableMapOf<String, MutableList<Long>>()

    private lateinit var chart: LineChart
    private lateinit var spinnerParam: Spinner
    private lateinit var btnParamInfo: ImageButton
    private lateinit var rvMeasurements: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var measurementAdapter: MeasurementAdapter
    private lateinit var dataRepo: DataRepository
    private lateinit var cardSpinner: MaterialCardView

    private var roomMetricJob: Job? = null
    private var spinnerItems: List<String> = emptyList()
    private var metricModels: List<MetricUiModel> = emptyList()
    private var currentMetricModel: MetricUiModel? = null
    private var currentRoomNorms: Pair<Float, Float>? = null
    private var selectedParamKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedParamKey = savedInstanceState?.getString(STATE_SELECTED_PARAM_KEY)

        dataRepo = DataRepository(requireContext())

        initViews(view)
        setupRecycler()
        loadData()
        setupChart()
        setupInfoButton()
        setupFab()
        applySafeArea(view)
        setupQuickAddResult()
        setupSpinnerTapArea()
        loadMetricModelsAndSetupSpinner()
    }


    override fun onResume() {
        super.onResume()
        refreshCurrentMetric()
    }

    override fun onDestroyView() {
        roomMetricJob?.cancel()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            STATE_SELECTED_PARAM_KEY,
            selectedParamKey ?: currentMetricModel?.key
        )
    }


    private fun initViews(root: View) {
        chart = root.findViewById(R.id.chartWbc)
        cardSpinner = root.findViewById(R.id.cardSpinner)
        spinnerParam = root.findViewById(R.id.spinnerParam)
        btnParamInfo = root.findViewById(R.id.btnParamInfo)
        rvMeasurements = root.findViewById(R.id.rvMeasurements)
        fabAdd = root.findViewById(R.id.fabAdd)
    }

    private fun setupRecycler() {
        measurementAdapter = MeasurementAdapter(
            measurements = mutableListOf(),
            onDelete = ::confirmDelete
        )

        rvMeasurements.layoutManager = LinearLayoutManager(requireContext())
        rvMeasurements.adapter = measurementAdapter
    }

    private fun loadData() {
        chartsData.clear()
        datesData.clear()

        chartsData.putAll(dataRepo.loadLegacyOnlyChartsData())
        datesData.putAll(dataRepo.loadLegacyOnlyDatesData())
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            val model = currentMetricModel ?: return@setOnClickListener

            QuickAddBottomSheet
                .newInstance(model.key)
                .show(parentFragmentManager, "QuickAddBottomSheet")
        }
    }

    private fun setupQuickAddResult() {
        parentFragmentManager.setFragmentResultListener(
            QuickAddBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val paramKey = bundle.getString(QuickAddBottomSheet.RESULT_PARAM)
                ?: return@setFragmentResultListener

            selectedParamKey = paramKey

            val selectedKey = currentMetricModel?.key

            if (selectedKey == paramKey) {
                refreshCurrentMetric(forceReloadLegacyCache = isLegacyMetric(paramKey))
                return@setFragmentResultListener
            }

            val index = metricModels.indexOfFirst { it.key == paramKey }
            if (index >= 0) {
                currentMetricModel = metricModels[index]
                spinnerParam.setSelection(index)
            }
        }
    }

    private fun loadMetricModelsAndSetupSpinner() {
        val app = requireActivity().application as App

        viewLifecycleOwner.lifecycleScope.launch {
            metricModels = try {
                app.appContainer.getHomeMetricsUseCase()
            } catch (_: Throwable) {
                emptyList()
            }

            spinnerItems = metricModels.map { it.displayName }

            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.item_spinner_param,
                spinnerItems
            )
            adapter.setDropDownViewResource(R.layout.item_spinner_param_dropdown)
            spinnerParam.adapter = adapter

            if (metricModels.isEmpty()) {
                currentMetricModel = null
                renderChartAndList(
                    param = "",
                    timestamps = emptyList(),
                    values = emptyList()
                )
                return@launch
            }

            val targetIndex = selectedParamKey
                ?.let { key -> metricModels.indexOfFirst { it.key == key } }
                ?.takeIf { it >= 0 }
                ?: metricModels.indexOfFirst { it.key == currentMetricModel?.key }
                    .takeIf { it >= 0 }
                ?: 0

            val targetModel = metricModels[targetIndex]
            currentMetricModel = targetModel
            selectedParamKey = targetModel.key

            spinnerParam.setSelection(targetIndex, false)

            spinnerParam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val model = metricModels.getOrNull(position) ?: return
                    if (currentMetricModel?.key == model.key) return

                    currentMetricModel = model
                    selectedParamKey = model.key
                    updateChart(model.key)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            refreshCurrentMetric(forceReloadLegacyCache = targetModel.source == MetricSource.LEGACY)
        }
    }

    private fun getMetricModel(param: String): MetricUiModel? {
        return metricModels.firstOrNull { it.key == param }
    }


    private fun setupInfoButton() {
        btnParamInfo.setOnClickListener {
            val model = currentMetricModel ?: return@setOnClickListener

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("${model.abbreviation} • ${model.displayName}")
                .setMessage(
                    "${model.description}\n\n${
                        presentationMapper.buildNormText(
                            model = model,
                            roomNorms = currentRoomNorms
                        )
                    }"
                )
                .setPositiveButton("Понятно", null)
                .show()
        }
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.setTouchEnabled(true)
        chart.isDragEnabled = true

        chart.setScaleXEnabled(true)
        chart.setScaleYEnabled(false)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false

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

    private fun updateChart(param: String) {
        roomMetricJob?.cancel()
        currentRoomNorms = null

        val model = getMetricModel(param)
        currentMetricModel = model

        if (model?.source == MetricSource.ROOM && model.roomMetricCode != null) {
            loadMetricFromRoom(param, model.roomMetricCode)
            return
        }

        if (MetricRegistry.isRoomBacked(param)) {
            return
        }

        renderLegacyMetric(param)
    }

    private fun renderLegacyMetric(param: String) {
        if (!dataRepo.isLegacyOnlyParam(param)) {
            return
        }

        val timestamps = datesData[param]?.toList().orEmpty()
        val values = chartsData[param]?.map { it.second }.orEmpty()

        renderChartAndList(param, timestamps, values)
    }

    private fun loadMetricFromRoom(param: String, metricCode: String) {
        val app = requireActivity().application as App

        roomMetricJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val model = getMetricModel(param) ?: return@launch

                currentRoomNorms = try {
                    app.appContainer.getReferenceRangeForActivePetUseCase(
                        metricCode = metricCode,
                        unit = model.unit
                    )
                } catch (_: Throwable) {
                    presentationMapper.resolveNorms(
                        model = model,
                        roomNorms = null
                    )
                }

                app.appContainer.observeMeasurementsByMetricCodeUseCase(metricCode)
                    .collect { measurements ->
                        renderRoomMeasurements(param, measurements)
                    }
            } catch (_: Throwable) {
            }
        }
    }

    private fun renderChartAndList(
        param: String,
        timestamps: List<Long>,
        values: List<Float>
    ) {
        val pairedData = timestamps.zip(values) { timestamp, value ->
            timestamp to value
        }.sortedBy { it.first }

        val sortedEntries = pairedData.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second)
        }

        val sortedTimestamps = pairedData.map { it.first }
        val model = getMetricModel(param)
        val paramColor = presentationMapper.resolveMetricColor(model)
        val norms = presentationMapper.resolveNorms(
            model = model,
            roomNorms = currentRoomNorms
        )

        setupYAxis(sortedEntries.map { it.y }, norms)
        setupXAxis(sortedEntries.size, sortedTimestamps)

        val mainDataSet = createMainDataSet(
            entries = sortedEntries,
            label = presentationMapper.resolveChartLabel(param, model),
            paramColor = paramColor
        )
        val fillDataSet = createNormFillDataSet(sortedEntries.size, norms)

        val allDataSets = mutableListOf<ILineDataSet>()
        fillDataSet?.let { allDataSets.add(it) }
        allDataSets.add(mainDataSet)

        chart.data = LineData(allDataSets)
        setupNormLines(norms)
        chart.invalidate()

        updateMeasurementsListFromRaw(param, pairedData)
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
        label: String,
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

        return LineDataSet(entries, label).apply {
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

    private fun updateMeasurementsListFromRaw(
        param: String,
        pairedData: List<Pair<Long, Float>>
    ) {
        val model = getMetricModel(param)

        val items = presentationMapper.mapMeasurementItems(
            param = param,
            model = model,
            roomNorms = currentRoomNorms,
            pairedData = pairedData
        )

        measurementAdapter.updateItems(items)
    }

    private fun deleteMeasurement(param: String, timestamp: Long) {
        val model = getMetricModel(param)
        val metricCode = model?.roomMetricCode ?: MetricRegistry.getMetricCodeOrNull(param)

        if (model?.source == MetricSource.ROOM && metricCode != null) {
            deleteRoomMeasurement(param, metricCode, timestamp)
            return
        }

        deleteLegacyMeasurement(param, timestamp)
    }

    private fun applySafeArea(root: View) {
        val main = root.findViewById<ConstraintLayout>(R.id.main)
        val fab = root.findViewById<FloatingActionButton>(R.id.fabAdd)

        ViewCompat.setOnApplyWindowInsetsListener(main) { view, insets ->
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun setupSpinnerTapArea() {
        cardSpinner.setOnClickListener {
            spinnerParam.performClick()
        }
    }

    private fun confirmDelete(item: MeasurementItem) {
        val model = currentMetricModel ?: return
        val param = model.key

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить запись?")
            .setMessage("${item.date} — ${item.value}")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                deleteMeasurement(param, item.timestamp)
            }
            .show()
    }

    private fun renderRoomMeasurements(param: String, items: List<MeasurementEntity>) {
        val pairs = items
            .mapNotNull { entity ->
                val value = entity.value?.toFloat() ?: return@mapNotNull null
                entity.measuredAt to value
            }

        val timestamps = pairs.map { it.first }
        val values = pairs.map { it.second }

        renderChartAndList(param, timestamps, values)
    }

    private fun isLegacyMetric(param: String): Boolean {
        val model = getMetricModel(param)
        return model?.source == MetricSource.LEGACY && !MetricRegistry.isRoomBacked(param)
    }

    private fun refreshCurrentMetric(forceReloadLegacyCache: Boolean = false) {
        val model = currentMetricModel ?: metricModels.firstOrNull() ?: return

        if (forceReloadLegacyCache || model.source == MetricSource.LEGACY) {
            loadData()
        }

        updateChart(model.key)
    }

    private fun deleteLegacyMeasurement(param: String, timestamp: Long) {
        val deleted = runCatching {
            dataRepo.deleteLegacyMeasurementByTimestamp(
                param = param,
                timestamp = timestamp
            )
        }.getOrDefault(false)

        if (deleted) {
            loadData()
            updateChart(param)
        }
    }

    private fun deleteRoomMeasurement(param: String, metricCode: String, timestamp: Long) {
        val app = requireActivity().application as App

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                app.appContainer.deleteMeasurementByMetricCodeForDayUseCase(
                    metricCode = metricCode,
                    measuredAt = timestamp
                )
                updateChart(param)
            } catch (_: Throwable) {
            }
        }
    }

    companion object {
        private const val STATE_SELECTED_PARAM_KEY = "state_selected_param_key"
    }

    private val presentationMapper = HomeMetricPresentationMapper()
}