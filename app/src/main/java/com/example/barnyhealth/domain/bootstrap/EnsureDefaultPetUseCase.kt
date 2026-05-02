package com.example.barnyhealth.domain.bootstrap

import kotlinx.coroutines.flow.first
import com.example.barnyhealth.data.local.db.dao.SpeciesDao
import com.example.barnyhealth.data.local.db.entity.SexType
import com.example.barnyhealth.data.local.db.entity.SpeciesType
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.PetRepository

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