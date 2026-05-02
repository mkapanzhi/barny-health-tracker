package com.example.barnyhealth.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MetricTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["metric_type_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pet_id"]),
        Index(value = ["metric_type_id"]),
        Index(value = ["pet_id", "metric_type_id", "measured_at"])
    ]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "pet_id")
    val petId: Long,

    @ColumnInfo(name = "metric_type_id")
    val metricTypeId: Long,

    @ColumnInfo(name = "value")
    val value: Double? = null,

    @ColumnInfo(name = "text_value")
    val textValue: String? = null,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "measured_at")
    val measuredAt: Long,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "source")
    val source: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)