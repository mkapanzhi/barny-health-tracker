package com.example.barnyhealth.data.local.db

import com.example.barnyhealth.data.local.db.entity.MetricTypeEntity
import com.example.barnyhealth.data.local.db.entity.ReferenceRangeEntity
import com.example.barnyhealth.data.local.db.entity.SpeciesEntity
import com.example.barnyhealth.data.local.db.entity.SpeciesType

class DatabaseSeeder(
    private val db: BarnyHealthDatabase
) {

    suspend fun seed() {
        db.metricTypeDao().insertAll(
            listOf(
                MetricTypeEntity(
                    code = "temperature",
                    displayName = "Температура",
                    defaultUnit = "°C",
                    category = "vitals",
                    isActive = true
                ),
                MetricTypeEntity(
                    code = "weight",
                    displayName = "Вес",
                    defaultUnit = "kg",
                    category = "body",
                    isActive = true
                ),
                MetricTypeEntity(
                    code = "glucose",
                    displayName = "Глюкоза",
                    defaultUnit = "mmol/L",
                    category = "biochemistry",
                    isActive = true
                ),
                MetricTypeEntity(
                    code = "urea",
                    displayName = "Мочевина",
                    defaultUnit = "mmol/L",
                    category = "biochemistry",
                    isActive = true
                ),
                MetricTypeEntity(
                    code = "creatinine",
                    displayName = "Креатинин",
                    defaultUnit = "µmol/L",
                    category = "biochemistry",
                    isActive = true
                )
            )
        )

        db.speciesDao().insertAll(
            listOf(
                SpeciesEntity(
                    code = SpeciesType.CAT,
                    displayName = "Кот"
                )
            )
        )

        val catSpecies = db.speciesDao().getByCode(SpeciesType.CAT) ?: return

        val temperatureMetric = db.metricTypeDao().getByCode("temperature") ?: return
        val weightMetric = db.metricTypeDao().getByCode("weight") ?: return
        val glucoseMetric = db.metricTypeDao().getByCode("glucose") ?: return
        val ureaMetric = db.metricTypeDao().getByCode("urea") ?: return
        val creatinineMetric = db.metricTypeDao().getByCode("creatinine") ?: return

        db.referenceRangeDao().insertAll(
            listOf(
                ReferenceRangeEntity(
                    speciesId = catSpecies.id,
                    metricTypeId = temperatureMetric.id,
                    unit = "°C",
                    minValue = 37.5,
                    maxValue = 39.5,
                    sourceName = "Seed",
                    note = "Базовая норма для кошки"
                ),
                ReferenceRangeEntity(
                    speciesId = catSpecies.id,
                    metricTypeId = weightMetric.id,
                    unit = "kg",
                    minValue = 0.5,
                    maxValue = 15.0,
                    sourceName = "Seed",
                    note = "Общий диапазон массы тела кошки"
                ),
                ReferenceRangeEntity(
                    speciesId = catSpecies.id,
                    metricTypeId = glucoseMetric.id,
                    unit = "mmol/L",
                    minValue = 3.3,
                    maxValue = 8.0,
                    sourceName = "Seed",
                    note = "Базовая норма для кошки"
                ),
                ReferenceRangeEntity(
                    speciesId = catSpecies.id,
                    metricTypeId = ureaMetric.id,
                    unit = "mmol/L",
                    minValue = 4.0,
                    maxValue = 8.0,
                    sourceName = "Seed",
                    note = "Базовая норма для кошки"
                ),
                ReferenceRangeEntity(
                    speciesId = catSpecies.id,
                    metricTypeId = creatinineMetric.id,
                    unit = "µmol/L",
                    minValue = 35.0,
                    maxValue = 140.0,
                    sourceName = "Seed",
                    note = "Базовая норма для кошки"
                )
            )
        )
    }
}