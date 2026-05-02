package com.example.barnyhealth.data.repository

import com.example.barnyhealth.data.local.db.dao.PetDao
import com.example.barnyhealth.data.local.db.entity.PetEntity
import com.example.barnyhealth.data.local.db.entity.SexType
import com.example.barnyhealth.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PetRepository(
    private val petDao: PetDao,
    private val settingsDataStore: SettingsDataStore
) {

    fun observeActivePets(): Flow<List<PetEntity>> = petDao.observeActivePets()

    suspend fun createPet(
        name: String,
        speciesId: Long,
        breed: String? = null,
        sex: SexType = SexType.UNKNOWN,
        birthDate: Long? = null,
        color: String? = null
    ): Long {
        val id = petDao.insert(
            PetEntity(
                name = name,
                speciesId = speciesId,
                breed = breed,
                sex = sex,
                birthDate = birthDate,
                color = color,
                isActive = true
            )
        )

        settingsDataStore.setActivePetId(id)
        return id
    }

    suspend fun getById(id: Long): PetEntity? = petDao.getById(id)

    suspend fun getActivePetOrNull(): PetEntity? {
        val activePetId = settingsDataStore.activePetIdFlow.first() ?: return null
        return petDao.getById(activePetId)
    }
}