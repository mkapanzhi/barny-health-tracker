package com.barny.healthtracker.domain

import com.barny.healthtracker.data.preferences.SettingsDataStore
import com.barny.healthtracker.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.first

class AddMeasurementByMetricCodeUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {
    suspend operator fun invoke(
        metricCode: String,
        value: Double,
        unit: String,
        measuredAt: Long,
        note: String? = null,
        source: String? = "quick_add"
    ): Long {
        val petId = settingsDataStore.activePetIdFlow.first()
            ?: error("Active pet is not selected")

        return measurementRepository.addOrReplaceMeasurementForDay(
            petId = petId,
            metricCode = metricCode,
            value = value,
            unit = unit,
            measuredAt = measuredAt,
            note = note,
            source = source
        )
    }
}