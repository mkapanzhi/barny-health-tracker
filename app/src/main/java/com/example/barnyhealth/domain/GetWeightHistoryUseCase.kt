package com.example.barnyhealth.domain

import kotlinx.coroutines.flow.first
import com.example.barnyhealth.data.local.db.entity.MeasurementEntity
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository

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