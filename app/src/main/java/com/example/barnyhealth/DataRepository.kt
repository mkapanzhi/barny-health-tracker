package com.example.barnyhealth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Calendar

data class LegacyMeasurementPoint(
    val key: String,
    val value: Float,
    val timestamp: Long
)

class DataRepository(private val context: Context) {

    private val chartsPrefs: SharedPreferences =
        context.getSharedPreferences("charts_prefs", Context.MODE_PRIVATE)

    private val datesPrefs: SharedPreferences =
        context.getSharedPreferences("dates_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    fun isLegacyOnlyParam(param: String): Boolean {
        return !MetricRegistry.isRoomBacked(param)
    }

    fun loadLegacyMeasurementPoints(): List<LegacyMeasurementPoint> {
        val chartsData = loadAllChartsData()
        val datesData = loadAllDatesData()

        return chartsData.flatMap { (key, values) ->
            val timestamps = datesData[key].orEmpty()

            values.mapIndexedNotNull { index, pair ->
                val timestamp = timestamps.getOrNull(index) ?: return@mapIndexedNotNull null
                val value = pair.second

                LegacyMeasurementPoint(
                    key = key,
                    value = value,
                    timestamp = timestamp
                )
            }
        }
    }

    fun upsertLegacyMeasurementForDay(
        param: String,
        timestamp: Long,
        value: Float
    ) {
        require(isLegacyOnlyParam(param)) {
            "Attempt to save room-backed metric into legacy storage: $param"
        }

        val chartsData = loadAllChartsData()
        val datesData = loadAllDatesData()

        val valuesList = chartsData.getOrPut(param) { mutableListOf() }
        val datesList = datesData.getOrPut(param) { mutableListOf() }

        val normalizedTimestamp = normalizeTimestamp(timestamp)

        val existingIndex = datesList.indexOfFirst { existingTimestamp ->
            normalizeTimestamp(existingTimestamp) == normalizedTimestamp
        }

        if (existingIndex != -1) {
            datesList[existingIndex] = normalizedTimestamp
            valuesList[existingIndex] = Pair(valuesList[existingIndex].first, value)
        } else {
            datesList.add(normalizedTimestamp)
            valuesList.add(Pair(valuesList.size.toFloat(), value))
        }

        saveAllChartsData(chartsData)
        saveAllDatesData(datesData)
    }

    fun deleteLegacyMeasurementByTimestamp(
        param: String,
        timestamp: Long
    ): Boolean {
        require(isLegacyOnlyParam(param)) {
            "Attempt to delete room-backed metric from legacy storage: $param"
        }

        val chartsData = loadAllChartsData()
        val datesData = loadAllDatesData()

        val paramDates = datesData[param]?.toMutableList() ?: return false
        val paramValues = chartsData[param]?.toMutableList() ?: return false

        val index = paramDates.indexOfFirst { it == timestamp }
        if (index == -1) return false

        paramDates.removeAt(index)
        if (index < paramValues.size) {
            paramValues.removeAt(index)
        }

        if (paramDates.isEmpty()) {
            datesData.remove(param)
        } else {
            datesData[param] = paramDates
        }

        if (paramValues.isEmpty()) {
            chartsData.remove(param)
        } else {
            chartsData[param] = paramValues
        }

        saveAllChartsData(chartsData)
        saveAllDatesData(datesData)

        return true
    }

    fun loadLegacyOnlyChartsData(): MutableMap<String, MutableList<Pair<Float, Float>>> {
        return loadAllChartsData()
            .filterKeys { isLegacyOnlyParam(it) }
            .mapValues { (_, value) -> value.toMutableList() }
            .toMutableMap()
    }

    fun loadLegacyOnlyDatesData(): MutableMap<String, MutableList<Long>> {
        return loadAllDatesData()
            .filterKeys { isLegacyOnlyParam(it) }
            .mapValues { (_, value) -> value.toMutableList() }
            .toMutableMap()
    }

    fun hasRoomBackedLegacyData(): Boolean {
        val chartsKeys = loadAllChartsData().keys
        val datesKeys = loadAllDatesData().keys
        return (chartsKeys + datesKeys).any { MetricRegistry.isRoomBacked(it) }
    }

    fun removeRoomBackedLegacyData() {
        val filteredCharts = loadAllChartsData()
            .filterKeys { !MetricRegistry.isRoomBacked(it) }
            .mapValues { (_, value) -> value.toMutableList() }
            .toMutableMap()

        val filteredDates = loadAllDatesData()
            .filterKeys { !MetricRegistry.isRoomBacked(it) }
            .mapValues { (_, value) -> value.toMutableList() }
            .toMutableMap()

        saveAllChartsData(filteredCharts)
        saveAllDatesData(filteredDates)
    }

    private fun saveAllChartsData(chartsData: MutableMap<String, MutableList<Pair<Float, Float>>>) {
        val json = gson.toJson(chartsData)
        chartsPrefs.edit().putString("chartsData", json).apply()
    }

    private fun loadAllChartsData(): MutableMap<String, MutableList<Pair<Float, Float>>> {
        val json = chartsPrefs.getString("chartsData", "{}") ?: "{}"
        val type: Type =
            object : TypeToken<MutableMap<String, MutableList<Pair<Float, Float>>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    private fun saveAllDatesData(datesData: MutableMap<String, MutableList<Long>>) {
        val json = gson.toJson(datesData)
        datesPrefs.edit().putString("datesData", json).apply()
    }

    private fun loadAllDatesData(): MutableMap<String, MutableList<Long>> {
        val json = datesPrefs.getString("datesData", "{}") ?: "{}"
        val type: Type = object : TypeToken<MutableMap<String, MutableList<Long>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    private fun normalizeTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}