package com.barny.healthtracker.domain

import kotlinx.coroutines.flow.first
import com.barny.healthtracker.data.local.db.entity.MeasurementEntity
import com.barny.healthtracker.data.preferences.SettingsDataStore
import com.barny.healthtracker.data.repository.MeasurementRepository

class GetWeightHistoryUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val measurementRepository: MeasurementRepository
) {

    suspend operator fun invoke(): List<MeasurementEntity> {
        val petId = settingsDataStore.activePetIdFlow.first()
            ?: return emptyList()

        return measurementRepository.getWeightHistory(petId)
    }
}