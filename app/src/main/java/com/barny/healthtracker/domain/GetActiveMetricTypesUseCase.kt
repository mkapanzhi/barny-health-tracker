package com.barny.healthtracker.domain

import com.barny.healthtracker.data.local.db.entity.MetricTypeEntity
import com.barny.healthtracker.data.repository.MeasurementRepository

class GetActiveMetricTypesUseCase(
    private val measurementRepository: MeasurementRepository
) {
    suspend operator fun invoke(): List<MetricTypeEntity> {
        return measurementRepository.getActiveMetricTypes()
    }
}