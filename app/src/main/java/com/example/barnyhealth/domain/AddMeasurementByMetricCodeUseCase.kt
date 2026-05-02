package com.example.barnyhealth.domain

import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
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