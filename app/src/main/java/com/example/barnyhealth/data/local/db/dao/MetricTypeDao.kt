package com.example.barnyhealth.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barnyhealth.data.local.db.entity.MetricTypeEntity

@Dao
interface MetricTypeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MetricTypeEntity>)

    @Query("SELECT * FROM metric_types WHERE is_active = 1 ORDER BY display_name ASC")
    suspend fun getAllActive(): List<MetricTypeEntity>

    @Query("SELECT * FROM metric_types WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): MetricTypeEntity?
}