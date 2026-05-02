package com.example.barnyhealth.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.barnyhealth.data.local.db.converter.RoomConverters
import com.example.barnyhealth.data.local.db.dao.MeasurementDao
import com.example.barnyhealth.data.local.db.dao.MetricTypeDao
import com.example.barnyhealth.data.local.db.dao.NoteDao
import com.example.barnyhealth.data.local.db.dao.PetDao
import com.example.barnyhealth.data.local.db.dao.ReferenceRangeDao
import com.example.barnyhealth.data.local.db.dao.SpeciesDao
import com.example.barnyhealth.data.local.db.entity.MeasurementEntity
import com.example.barnyhealth.data.local.db.entity.MetricTypeEntity
import com.example.barnyhealth.data.local.db.entity.NoteEntity
import com.example.barnyhealth.data.local.db.entity.PetEntity
import com.example.barnyhealth.data.local.db.entity.ReferenceRangeEntity
import com.example.barnyhealth.data.local.db.entity.SpeciesEntity

@Database(
    entities = [
        SpeciesEntity::class,
        PetEntity::class,
        MetricTypeEntity::class,
        MeasurementEntity::class,
        ReferenceRangeEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class BarnyHealthDatabase : RoomDatabase() {
    abstract fun speciesDao(): SpeciesDao
    abstract fun petDao(): PetDao
    abstract fun metricTypeDao(): MetricTypeDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun referenceRangeDao(): ReferenceRangeDao
    abstract fun noteDao(): NoteDao
}