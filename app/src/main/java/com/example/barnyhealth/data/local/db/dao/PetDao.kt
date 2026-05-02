package com.example.barnyhealth.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.barnyhealth.data.local.db.entity.PetEntity

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PetEntity): Long

    @Query("SELECT * FROM pets WHERE is_active = 1 ORDER BY name ASC")
    fun observeActivePets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getById(petId: Long): PetEntity?
}