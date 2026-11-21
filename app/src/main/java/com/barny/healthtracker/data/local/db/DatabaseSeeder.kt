package com.barny.healthtracker.data.local.db

import androidx.room.withTransaction
import com.barny.healthtracker.HealthParams
import com.barny.healthtracker.PetReferenceIntervals
import com.barny.healthtracker.PetSpecies
import com.barny.healthtracker.data.local.db.entity.MetricTypeEntity
import com.barny.healthtracker.data.local.db.entity.ReferenceRangeEntity
import com.barny.healthtracker.data.local.db.entity.SpeciesEntity
import com.barny.healthtracker.data.local.db.entity.SpeciesType

class DatabaseSeeder(private val db: BarnyHealthDatabase) {
    suspend fun seed() {
        db.withTransaction {
            val definitions = listOf(
                Triple("temperature", "Температура", "°C"),
                Triple("weight", "Вес", "kg"),
                Triple("glucose", "Глюкоза", "mmol/L"),
                Triple("urea", "Мочевина", "mmol/L"),
                Triple("creatinine", "Креатинин", "µmol/L")
            )
            db.metricTypeDao().insertAll(definitions.map { (code, name, unit) ->
                MetricTypeEntity(code = code, displayName = name, defaultUnit = unit,
                    category = if (code == "temperature") "vitals" else if (code == "weight") "body" else "biochemistry",
                    isActive = true)
            })
            db.speciesDao().insertAll(listOf(
                SpeciesEntity(code = SpeciesType.CAT, displayName = "Кот"),
                SpeciesEntity(code = SpeciesType.DOG, displayName = "Собака")
            ))
            for ((type, species) in listOf(SpeciesType.CAT to PetSpecies.CAT, SpeciesType.DOG to PetSpecies.DOG)) {
                val speciesId = checkNotNull(db.speciesDao().getByCode(type)).id
                val rows = definitions.map { (code, name, unit) ->
                    val metricId = checkNotNull(db.metricTypeDao().getByCode(code)).id
                    val laboratory = PetReferenceIntervals.laboratoryInterval(name, species)
                    ReferenceRangeEntity(
                        speciesId = speciesId, metricTypeId = metricId, unit = unit,
                        minValue = laboratory?.min?.toDouble() ?: if (code == "temperature") 37.5 else null,
                        maxValue = laboratory?.max?.toDouble() ?: if (code == "temperature") 39.5 else null,
                        sourceName = if (laboratory != null) "Cornell AHDC Cobas 501" else "App baseline",
                        note = if (code == "weight") "Универсальной нормы массы нет; учитывайте породу, возраст и упитанность."
                            else if (code == "temperature") "Сохранён исходный ориентир приложения; не диагностический порог."
                            else "Ориентировочный интервал. Приоритет у референса на бланке лаборатории."
                    )
                }
                db.referenceRangeDao().insertAll(rows)
            }
        }
    }
}
