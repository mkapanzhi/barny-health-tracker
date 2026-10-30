package com.barny.healthtracker.domain

import com.barny.healthtracker.data.preferences.SettingsDataStore
import com.barny.healthtracker.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.first

class DeleteMeasurementByMetricCodeForDayUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {

    suspend operator fun invoke(
        metricCode: String,
        measuredAt: Long
    ): Int {
        val petId = settingsDataStore.activePetIdFlow.first() ?: return 0

        return measurementRepository.deleteMeasurementByMetricAndDay(
            petId = petId,
            metricCode = metricCode,
            measuredAt = measuredAt
        )
    }
}