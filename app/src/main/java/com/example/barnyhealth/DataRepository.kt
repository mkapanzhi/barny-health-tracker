package com.example.barnyhealth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class DataRepository(private val context: Context) {

    private val chartsPrefs: SharedPreferences =
        context.getSharedPreferences("charts_prefs", Context.MODE_PRIVATE)

    private val datesPrefs: SharedPreferences =
        context.getSharedPreferences("dates_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveChartsData(chartsData: MutableMap<String, MutableList<Pair<Float, Float>>>) {
        val json = gson.toJson(chartsData)
        chartsPrefs.edit().putString("chartsData", json).apply()
    }

    fun loadChartsData(): MutableMap<String, MutableList<Pair<Float, Float>>> {
        val json = chartsPrefs.getString("chartsData", "{}") ?: "{}"
        val type: Type =
            object : TypeToken<MutableMap<String, MutableList<Pair<Float, Float>>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    fun saveDatesData(datesData: MutableMap<String, MutableList<Long>>) {
        val json = gson.toJson(datesData)
        datesPrefs.edit().putString("datesData", json).apply()
    }

    fun loadDatesData(): MutableMap<String, MutableList<Long>> {
        val json = datesPrefs.getString("datesData", "{}") ?: "{}"
        val type: Type = object : TypeToken<MutableMap<String, MutableList<Long>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
}
