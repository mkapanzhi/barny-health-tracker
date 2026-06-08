package com.example.barnyhealth.domain

import com.example.barnyhealth.DataRepository
import com.example.barnyhealth.MetricRegistry
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.first

class ImportLegacyMeasurementsUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository,
    private val dataRepository: DataRepository
) {

    suspend operator fun invoke() {
        val alreadyImported = settingsDataStore.legacyImportDoneFlow.first()
        if (alreadyImported) return

        val activePetId = settingsDataStore.activePetIdFlow.first() ?: return

        val legacyPoints = dataRepository.loadLegacyMeasurementPoints()
        if (legacyPoints.isEmpty()) {
            settingsDataStore.setLegacyImportDone(true)
            return
        }

        val roomBackedPoints = legacyPoints.mapNotNull { point ->
            val roomConfig = MetricRegistry.getRoomMetricConfig(point.key) ?: return@mapNotNull null
            point to roomConfig
        }

        if (roomBackedPoints.isEmpty()) {
            settingsDataStore.setLegacyImportDone(true)
            return
        }

        roomBackedPoints.forEach { (point, roomConfig) ->
            measurementRepository.addOrReplaceMeasurementForDay(
                petId = activePetId,
                metricCode = roomConfig.metricCode,
                value = point.value.toDouble(),
                unit = roomConfig.unit,
                measuredAt = point.timestamp,
                note = null,
                source = "legacy_import"
            )
        }

        settingsDataStore.setLegacyImportDone(true)
    }
}