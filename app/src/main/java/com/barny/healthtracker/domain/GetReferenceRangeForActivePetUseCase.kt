package com.barny.healthtracker.domain

import com.barny.healthtracker.data.repository.MeasurementRepository
import com.barny.healthtracker.data.repository.PetRepository

class GetReferenceRangeForActivePetUseCase(
    private val petRepository: PetRepository,
    private val measurementRepository: MeasurementRepository
) {
    suspend operator fun invoke(
        metricCode: String,
        unit: String
    ): Pair<Float, Float>? {
        val activePet = petRepository.getActivePetOrNull() ?: return null

        val range = measurementRepository.getReferenceRangeForSpeciesAndMetricCode(
            speciesId = activePet.speciesId,
            metricCode = metricCode,
            unit = unit
        ) ?: return null

        val min = range.minValue?.toFloat() ?: return null
        val max = range.maxValue?.toFloat() ?: return null

        return min to max
    }
}