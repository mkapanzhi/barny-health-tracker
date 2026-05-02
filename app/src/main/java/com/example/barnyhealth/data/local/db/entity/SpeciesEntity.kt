package com.example.barnyhealth.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "species",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class SpeciesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: SpeciesType,

    @ColumnInfo(name = "display_name")
    val displayName: String
)