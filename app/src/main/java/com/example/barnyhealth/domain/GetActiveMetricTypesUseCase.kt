package com.example.barnyhealth.domain

import com.example.barnyhealth.data.local.db.entity.MetricTypeEntity
import com.example.barnyhealth.data.repository.MeasurementRepository

class GetActiveMetricTypesUseCase(
    private val measurementRepository: MeasurementRepository
) {
    suspend operator fun invoke(): List<MetricTypeEntity> {
        return measurementRepository.getActiveMetricTypes()
    }
}