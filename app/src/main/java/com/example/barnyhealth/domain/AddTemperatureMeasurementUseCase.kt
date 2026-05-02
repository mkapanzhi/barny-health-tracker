package com.example.barnyhealth.com.example.barnyhealth.domain

import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.first

class AddTemperatureMeasurementUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {

    suspend operator fun invoke(
        temperatureC: Double,
        measuredAt: Long,
        note: String? = null
    ): Long {
        val petId = settingsDataStore.activePetIdFlow.first()
            ?: error("Active pet is not selected")

        return measurementRepository.addOrReplaceMeasurementByDay(
            petId = petId,
            metricCode = "temperature",
            value = temperatureC,
            unit = "°C",
            measuredAt = measuredAt,
            note = note,
            source = "quick_add"
        )
    }
}