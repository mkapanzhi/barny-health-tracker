package com.example.barnyhealth.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.barnyhealth.data.local.db.entity.MeasurementEntity

@Dao
interface MeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MeasurementEntity): Long

    @Update
    suspend fun update(item: MeasurementEntity)

    @Query("""
        SELECT * FROM measurements
        WHERE pet_id = :petId AND metric_type_id = :metricTypeId
        ORDER BY measured_at ASC
    """)
    suspend fun getByPetAndMetric(
        petId: Long,
        metricTypeId: Long
    ): List<MeasurementEntity>

    @Query("""
        SELECT * FROM measurements
        WHERE pet_id = :petId AND metric_type_id = :metricTypeId
        ORDER BY measured_at ASC
    """)
    fun observeByPetAndMetric(
        petId: Long,
        metricTypeId: Long
    ): Flow<List<MeasurementEntity>>

    @Query("""
        SELECT * FROM measurements
        WHERE pet_id = :petId
          AND metric_type_id = :metricTypeId
          AND measured_at BETWEEN :dayStart AND :dayEnd
        LIMIT 1
    """)
    suspend fun getByPetMetricAndDay(
        petId: Long,
        metricTypeId: Long,
        dayStart: Long,
        dayEnd: Long
    ): MeasurementEntity?

    @Query("""
        DELETE FROM measurements
        WHERE pet_id = :petId
          AND metric_type_id = :metricTypeId
          AND measured_at = :measuredAt
    """)
    suspend fun deleteByPetMetricAndMeasuredAt(
        petId: Long,
        metricTypeId: Long,
        measuredAt: Long
    ): Int
}