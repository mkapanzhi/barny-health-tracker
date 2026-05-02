package com.example.barnyhealth.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reference_ranges",
    foreignKeys = [
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["species_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MetricTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["metric_type_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["species_id"]),
        Index(value = ["metric_type_id"]),
        Index(value = ["species_id", "metric_type_id", "unit"], unique = true)
    ]
)
data class ReferenceRangeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "species_id")
    val speciesId: Long,

    @ColumnInfo(name = "metric_type_id")
    val metricTypeId: Long,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "min_value")
    val minValue: Double? = null,

    @ColumnInfo(name = "max_value")
    val maxValue: Double? = null,

    @ColumnInfo(name = "source_name")
    val sourceName: String? = null,

    @ColumnInfo(name = "note")
    val note: String? = null
)