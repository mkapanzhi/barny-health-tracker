package com.barny.healthtracker.domain.bootstrap

import kotlinx.coroutines.flow.first
import com.barny.healthtracker.data.local.db.dao.SpeciesDao
import com.barny.healthtracker.data.local.db.entity.SexType
import com.barny.healthtracker.data.local.db.entity.SpeciesType
import com.barny.healthtracker.data.preferences.SettingsDataStore
import com.barny.healthtracker.data.repository.PetRepository

class EnsureDefaultPetUseCase(
    private val speciesDao: SpeciesDao,
    private val petRepository: PetRepository,
    private val settingsDataStore: SettingsDataStore
) {

    suspend operator fun invoke() {
        val activePetId = settingsDataStore.activePetIdFlow.first()
        if (activePetId != null) return

        val catSpecies = speciesDao.getByCode(SpeciesType.CAT) ?: return

        petRepository.createPet(
            name = "Barny",
            speciesId = catSpecies.id,
            breed = null,
            sex = SexType.MALE,
            birthDate = null,
            color = null
        )
    }
}