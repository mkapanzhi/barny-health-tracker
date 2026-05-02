package com.example.barnyhealth.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barnyhealth.data.local.db.entity.ReferenceRangeEntity

@Dao
interface ReferenceRangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ReferenceRangeEntity>)

    @Query("""
        SELECT * FROM reference_ranges
        WHERE species_id = :speciesId
          AND metric_type_id = :metricTypeId
          AND unit = :unit
        LIMIT 1
    """)
    suspend fun getForSpeciesMetricUnit(
        speciesId: Long,
        metricTypeId: Long,
        unit: String
    ): ReferenceRangeEntity?
}