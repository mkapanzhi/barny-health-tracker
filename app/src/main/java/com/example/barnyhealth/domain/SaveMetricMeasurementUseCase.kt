package com.example.barnyhealth.domain

import com.example.barnyhealth.DataRepository
import com.example.barnyhealth.domain.model.MetricSource
import com.example.barnyhealth.domain.model.MetricUiModel

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
        when (model?.source) {
            MetricSource.ROOM -> {
                val metricCode = model.roomMetricCode
                    ?: error("Room metric code is missing for ${model.key}")

                addMeasurementByMetricCodeUseCase(
                    metricCode = metricCode,
                    value = value.toDouble(),
                    unit = model.unit,
                    measuredAt = measuredAt,
                    note = null,
                    source = source
                )
            }

            MetricSource.LEGACY,
            null -> {
                dataRepository.upsertLegacyMeasurementForDay(
                    param = fallbackParamKey,
                    timestamp = measuredAt,
                    value = value
                )
            }
        }
    }
}