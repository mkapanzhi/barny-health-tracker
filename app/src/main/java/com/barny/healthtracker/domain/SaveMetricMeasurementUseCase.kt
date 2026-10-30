package com.barny.healthtracker.domain

import com.barny.healthtracker.DataRepository
import com.barny.healthtracker.domain.model.MetricSource
import com.barny.healthtracker.domain.model.MetricUiModel

class SaveMetricMeasurementUseCase(
    private val dataRepository: DataRepository,
    private val addMeasurementByMetricCodeUseCase: AddMeasurementByMetricCodeUseCase
) {
    suspend operator fun invoke(
        model: MetricUiModel?,
        fallbackParamKey: String,
        value: Float,
        measuredAt: Long,
        source: String
    ) {
        val metricModel = model
            ?: error("Metric model is missing for param: $fallbackParamKey")

        when (metricModel.source) {
            MetricSource.ROOM -> {
                val metricCode = metricModel.roomMetricCode?.takeIf { it.isNotBlank() }
                    ?: error("Room metric code is missing or blank for ${metricModel.key}")

                addMeasurementByMetricCodeUseCase(
                    metricCode = metricCode,
                    value = value.toDouble(),
                    unit = metricModel.unit,
                    measuredAt = measuredAt,
                    note = null,
                    source = source
                )
            }

            MetricSource.LEGACY -> {
                dataRepository.upsertLegacyMeasurementForDay(
                    param = metricModel.key,
                    timestamp = measuredAt,
                    value = value
                )
            }
        }
    }
}