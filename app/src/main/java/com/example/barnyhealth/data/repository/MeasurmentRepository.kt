package com.example.barnyhealth.data.repository

import com.example.barnyhealth.data.local.db.dao.MeasurementDao
import com.example.barnyhealth.data.local.db.dao.MetricTypeDao
import com.example.barnyhealth.data.local.db.dao.ReferenceRangeDao
import com.example.barnyhealth.data.local.db.entity.MeasurementEntity
import com.example.barnyhealth.data.local.db.entity.MetricTypeEntity
import com.example.barnyhealth.data.local.db.entity.ReferenceRangeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class MeasurementRepository(
    private val measurementDao: MeasurementDao,
    private val metricTypeDao: MetricTypeDao,
    private val referenceRangeDao: ReferenceRangeDao
) {

    suspend fun addMeasurement(
        petId: Long,
        metricCode: String,
        value: Double,
        unit: String,
        measuredAt: Long,
        note: String? = null,
        source: String? = null
    ): Long {
        val metricType = metricTypeDao.getByCode(metricCode)
            ?: error("Metric type not found: $metricCode")

        return measurementDao.insert(
            MeasurementEntity(
                petId = petId,
                metricTypeId = metricType.id,
                value = value,
                textValue = null,
                unit = unit,
                measuredAt = measuredAt,
                note = note,
                source = source
            )
        )
    }

    suspend fun addOrReplaceMeasurementForDay(
        petId: Long,
        metricCode: String,
        value: Double,
        unit: String,
        measuredAt: Long,
        note: String? = null,
        source: String? = null
    ): Long {
        val metricType = metricTypeDao.getByCode(metricCode)
            ?: error("Metric type not found: $metricCode")

        val (dayStart, dayEnd) = dayBounds(measuredAt)

        val existing = measurementDao.getByPetMetricAndDay(
            petId = petId,
            metricTypeId = metricType.id,
            dayStart = dayStart,
            dayEnd = dayEnd
        )

        return if (existing != null) {
            measurementDao.update(
                existing.copy(
                    value = value,
                    unit = unit,
                    measuredAt = measuredAt,
                    note = note,
                    source = source
                )
            )
            existing.id
        } else {
            measurementDao.insert(
                MeasurementEntity(
                    petId = petId,
                    metricTypeId = metricType.id,
                    value = value,
                    textValue = null,
                    unit = unit,
                    measuredAt = measuredAt,
                    note = note,
                    source = source
                )
            )
        }
    }

    suspend fun getMeasurementsByMetric(
        petId: Long,
        metricCode: String
    ): List<MeasurementEntity> {
        val metricType = metricTypeDao.getByCode(metricCode)
            ?: return emptyList()

        return measurementDao.getByPetAndMetric(
            petId = petId,
            metricTypeId = metricType.id
        )
    }

    suspend fun deleteMeasurementByMetricAndTimestamp(
        petId: Long,
        metricCode: String,
        measuredAt: Long
    ): Int {
        val metricType = metricTypeDao.getByCode(metricCode)
            ?: return 0

        return measurementDao.deleteByPetMetricAndMeasuredAt(
            petId = petId,
            metricTypeId = metricType.id,
            measuredAt = measuredAt
        )
    }

    suspend fun deleteMeasurementByMetricAndDay(
        petId: Long,
        metricCode: String,
        measuredAt: Long
    ): Int {
        val metricType = metricTypeDao.getByCode(metricCode)
            ?: return 0

        val (dayStart, dayEnd) = dayBounds(measuredAt)

        return measurementDao.deleteByPetMetricAndDay(
            petId = petId,
            metricTypeId = metricType.id,
            dayStart = dayStart,
            dayEnd = dayEnd
        )
    }

    suspend fun getWeightHistory(petId: Long): List<MeasurementEntity> {
        return getMeasurementsByMetric(
            petId = petId,
            metricCode = "weight"
        )
    }

    suspend fun getActiveMetricTypes(): List<MetricTypeEntity> {
        return metricTypeDao.getAllActive()
    }

    suspend fun getReferenceRangeForSpeciesAndMetricCode(
        speciesId: Long,
        metricCode: String,
        unit: String
    ): ReferenceRangeEntity? {
        val metricType = metricTypeDao.getByCode(metricCode) ?: return null
        return referenceRangeDao.getForSpeciesMetricUnit(
            speciesId = speciesId,
            metricTypeId = metricType.id,
            unit = unit
        )
    }

    fun observeMeasurementsByMetric(
        petId: Long,
        metricTypeId: Long
    ): Flow<List<MeasurementEntity>> {
        return measurementDao.observeByPetAndMetric(
            petId = petId,
            metricTypeId = metricTypeId
        )
    }

    private fun dayBounds(timestamp: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis

        return start to end
    }

    suspend fun addOrReplaceMeasurementByDay(
        petId: Long,
        metricCode: String,
        value: Double,
        unit: String,
        measuredAt: Long,
        note: String? = null,
        source: String? = null
    ): Long {
        return addOrReplaceMeasurementForDay(
            petId = petId,
            metricCode = metricCode,
            value = value,
            unit = unit,
            measuredAt = measuredAt,
            note = note,
            source = source
        )
    }
}