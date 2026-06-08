package com.example.barnyhealth

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryListActivity : AppCompatActivity() {

    private lateinit var adapter: ListAdapter
    private lateinit var dataRepo: DataRepository

    private val norms = HealthParams.NORMS
    private var selectedParams = setOf("WBC", "ALT")
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_list)

        dataRepo = DataRepository(this)

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        val checkWBC = findViewById<CheckBox>(R.id.checkWBC)
        val checkALT = findViewById<CheckBox>(R.id.checkALT)

        adapter = ListAdapter(norms) { item -> onDelete(item) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupCheckboxes(checkWBC, checkALT)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        loadHistory()
    }

    private fun setupCheckboxes(checkWBC: CheckBox, checkALT: CheckBox) {
        checkWBC.setOnCheckedChangeListener { _, isChecked ->
            selectedParams = if (isChecked) selectedParams + "WBC" else selectedParams - "WBC"
            loadHistory()
        }
        checkALT.setOnCheckedChangeListener { _, isChecked ->
            selectedParams = if (isChecked) selectedParams + "ALT" else selectedParams - "ALT"
            loadHistory()
        }
    }

    private fun loadHistory(): List<ListItem> {
        val chartsData = dataRepo.loadLegacyOnlyChartsData()
        val datesData = dataRepo.loadLegacyOnlyDatesData()

        val allItems = mutableListOf<ListItem>()

        selectedParams.forEach { param ->
            if (!dataRepo.isLegacyOnlyParam(param)) return@forEach

            val points = chartsData[param].orEmpty()
            val timestamps = datesData[param].orEmpty()

            val paired = points.zip(timestamps) { point, timestamp ->
                ListItem(param, point.second, timestamp)
            }

            allItems.addAll(paired)
        }

        val sortedItems = allItems.sortedByDescending { it.timestamp }
        adapter.updateItems(sortedItems)
        return sortedItems
    }

    private fun onDelete(item: ListItem) {
        if (!dataRepo.isLegacyOnlyParam(item.param)) {
            Toast.makeText(
                this,
                "Эта запись хранится в Room и не удаляется через legacy history",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val deleted = dataRepo.deleteLegacyMeasurementByTimestamp(
            param = item.param,
            timestamp = item.timestamp
        )

        if (deleted) {
            Toast.makeText(this, "Удалено из ${item.param}", Toast.LENGTH_SHORT).show()
            loadHistory()
        } else {
            Toast.makeText(this, "Не удалось удалить запись", Toast.LENGTH_SHORT).show()
        }
    }
}