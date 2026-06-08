package com.example.barnyhealth.domain

import com.example.barnyhealth.DataRepository
import com.example.barnyhealth.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.first

class CleanupMigratedLegacyMeasurementsUseCase(
    private val settingsDataStore: SettingsDataStore,
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke() {
        val importDone = settingsDataStore.legacyImportDoneFlow.first()
        if (!importDone) return

        val cleanupDone = settingsDataStore.legacyCleanupDoneFlow.first()
        if (cleanupDone) return

        if (dataRepository.hasRoomBackedLegacyData()) {
            dataRepository.removeRoomBackedLegacyData()
        }

        settingsDataStore.setLegacyCleanupDone(true)
    }
}