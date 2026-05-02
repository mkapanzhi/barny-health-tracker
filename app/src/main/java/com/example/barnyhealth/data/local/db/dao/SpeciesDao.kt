package com.example.barnyhealth.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barnyhealth.data.local.db.entity.SpeciesEntity
import com.example.barnyhealth.data.local.db.entity.SpeciesType

@Dao
interface SpeciesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<SpeciesEntity>)

    @Query("SELECT * FROM species ORDER BY display_name ASC")
    suspend fun getAll(): List<SpeciesEntity>

    @Query("SELECT * FROM species WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: SpeciesType): SpeciesEntity?
}