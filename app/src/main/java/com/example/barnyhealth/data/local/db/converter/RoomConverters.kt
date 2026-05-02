package com.example.barnyhealth.data.local.db.converter

import androidx.room.TypeConverter
import com.example.barnyhealth.data.local.db.entity.MetricValueType
import com.example.barnyhealth.data.local.db.entity.SexType
import com.example.barnyhealth.data.local.db.entity.SpeciesType

class RoomConverters {

    @TypeConverter
    fun fromSpeciesType(value: SpeciesType?): String? = value?.name

    @TypeConverter
    fun toSpeciesType(value: String?): SpeciesType? =
        value?.let { SpeciesType.valueOf(it) }

    @TypeConverter
    fun fromSexType(value: SexType?): String? = value?.name

    @TypeConverter
    fun toSexType(value: String?): SexType? =
        value?.let { SexType.valueOf(it) }

    @TypeConverter
    fun fromMetricValueType(value: MetricValueType?): String? = value?.name

    @TypeConverter
    fun toMetricValueType(value: String?): MetricValueType? =
        value?.let { MetricValueType.valueOf(it) }
}