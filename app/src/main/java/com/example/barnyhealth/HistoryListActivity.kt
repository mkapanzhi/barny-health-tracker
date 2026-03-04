package com.example.barnyhealth

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.*

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
        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        val checkWBC = findViewById<CheckBox>(R.id.checkWBC)
        val checkALT = findViewById<CheckBox>(R.id.checkALT)

        adapter = ListAdapter(norms) { param, position -> onDelete(param, position) }
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
        val chartsData = dataRepo.loadChartsData()
        val datesData = dataRepo.loadDatesData() // ✅ Загружаем ДАТЫ!

        val allItems = mutableListOf<ListItem>()

        selectedParams.forEach { param ->
            val points = chartsData[param] ?: emptyList()
            val timestamps = datesData[param] ?: emptyList()

            // ✅ Синхронизируем по порядку добавления
            val paired = points.zip(timestamps) { point, timestamp ->
                ListItem(param, point.second, timestamp)
            }

            allItems.addAll(paired)
        }

        val sortedItems = allItems.sortedByDescending { it.timestamp }
        adapter.updateItems(sortedItems)
        return sortedItems
    }

    private fun onDelete(param: String, position: Int) {
        // ✅ Загружаем АКТУАЛЬНЫЕ данные
        val chartsData = dataRepo.loadChartsData().toMutableMap()
        val datesData = dataRepo.loadDatesData().toMutableMap()

        // ✅ Удаляем ИЗ ОБОИХ списков по тому же индексу!
        chartsData[param]?.removeAt(position)
        datesData[param]?.removeAt(position)

        // ✅ СОХРАНЯЕМ изменения
        dataRepo.saveChartsData(chartsData)
        dataRepo.saveDatesData(datesData)

        Toast.makeText(this, "🗑️ Удалено из $param", Toast.LENGTH_SHORT).show()
        loadHistory()
    }
}
