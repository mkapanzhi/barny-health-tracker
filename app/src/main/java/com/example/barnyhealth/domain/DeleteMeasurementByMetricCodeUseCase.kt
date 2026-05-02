package com.example.barnyhealth.domain

import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.first

class DeleteMeasurementByMetricCodeUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {
    suspend operator fun invoke(
        metricCode: String,
        measuredAt: Long
    ): Int {
        val petId = settingsDataStore.activePetIdFlow.first()
            ?: error("Active pet is not selected")

        return measurementRepository.deleteMeasurementByMetricAndTimestamp(
            petId = petId,
            metricCode = metricCode,
            measuredAt = measuredAt
        )
    }
}