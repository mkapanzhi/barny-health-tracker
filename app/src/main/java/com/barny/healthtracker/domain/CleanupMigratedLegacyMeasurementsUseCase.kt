package com.barny.healthtracker.domain

import com.barny.healthtracker.DataRepository
import com.barny.healthtracker.data.preferences.SettingsDataStore
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