package com.example.barnyhealth.domain

import kotlinx.coroutines.flow.first
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository

class AddWeightMeasurementUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {

    suspend operator fun invoke(
        weightKg: Double,
        measuredAt: Long,
        note: String? = null
    ): Long {
        val petId = settingsDataStore.activePetIdFlow.first()
            ?: error("Active pet is not selected")

        return measurementRepository.addOrReplaceMeasurementForDay(
            petId = petId,
            metricCode = "weight",
            value = weightKg,
            unit = "kg",
            measuredAt = measuredAt,
            note = note,
            source = "quick_add"
        )
    }
}