package com.example.barnyhealth.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "metric_types",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class MetricTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "default_unit")
    val defaultUnit: String,

    @ColumnInfo(name = "value_type")
    val valueType: MetricValueType = MetricValueType.DECIMAL,

    @ColumnInfo(name = "category")
    val category: String? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)