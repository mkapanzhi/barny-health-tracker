package com.example.barnyhealth.domain

import com.example.barnyhealth.data.local.db.entity.MeasurementEntity
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ObserveMeasurementsByMetricCodeUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {
    operator fun invoke(metricCode: String): Flow<List<MeasurementEntity>> = flow {
        val petId = settingsDataStore.activePetIdFlow.first()
            ?: error("Active pet is not selected")

        val items = measurementRepository.getMeasurementsByMetric(
            petId = petId,
            metricCode = metricCode
        )

        emit(items)
    }
}