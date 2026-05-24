package com.example.barnyhealth.domain

import android.graphics.Color
import com.example.barnyhealth.LegacyMetricMetadataProvider
import com.example.barnyhealth.MetricRegistry
import com.example.barnyhealth.data.repository.MeasurementRepository
import com.example.barnyhealth.data.repository.PetRepository
import com.example.barnyhealth.domain.model.MetricSource
import com.example.barnyhealth.domain.model.MetricUiModel

class GetHomeMetricsUseCase(
    private val petRepository: PetRepository,
    private val measurementRepository: MeasurementRepository,
    private val legacyMetricMetadataProvider: LegacyMetricMetadataProvider
) {

    suspend operator fun invoke(): List<MetricUiModel> {
        val result = mutableListOf<MetricUiModel>()

        val activePet = petRepository.getActivePetOrNull()
        val activeSpeciesId = activePet?.speciesId

        val roomMetrics = measurementRepository.getActiveMetricTypes()

        roomMetrics.forEach { metricType ->
            val key = MetricRegistry.paramKeyForMetricCode(metricType.code) ?: return@forEach
            val legacyMetadata = legacyMetricMetadataProvider.getByKey(key)
            val unit = metricType.defaultUnit ?: MetricRegistry.getUnitOrNull(key) ?: legacyMetadata?.unit ?: ""

            val range = if (activeSpeciesId != null && unit.isNotBlank()) {
                measurementRepository.getReferenceRangeForSpeciesAndMetricCode(
                    speciesId = activeSpeciesId,
                    metricCode = metricType.code,
                    unit = unit
                )
            } else {
                null
            }

            result += MetricUiModel(
                key = key,
                displayName = metricType.displayName.ifBlank { legacyMetadata?.displayName ?: key },
                abbreviation = legacyMetadata?.abbreviation ?: metricType.displayName,
                description = legacyMetadata?.description ?: "Описание пока не добавлено.",
                color = legacyMetadata?.color ?: Color.GRAY,
                unit = unit,
                normMin = range?.minValue?.toFloat(),
                normMax = range?.maxValue?.toFloat(),
                source = MetricSource.ROOM,
                roomMetricCode = metricType.code
            )
        }

        val roomKeys = result.map { it.key }.toSet()

        val legacyKeys = com.example.barnyhealth.HealthParams.ALL_PARAMS
            .filterNot { key -> key in roomKeys }

        legacyKeys.forEach { key ->
            val metadata = legacyMetricMetadataProvider.getByKey(key) ?: return@forEach

            result += MetricUiModel(
                key = key,
                displayName = metadata.displayName,
                abbreviation = metadata.abbreviation,
                description = metadata.description,
                color = metadata.color,
                unit = metadata.unit,
                normMin = metadata.normMin,
                normMax = metadata.normMax,
                source = MetricSource.LEGACY,
                roomMetricCode = null
            )
        }

        return result
    }
}