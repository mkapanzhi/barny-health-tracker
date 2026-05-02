package com.example.barnyhealth.app

import android.content.Context
import com.example.barnyhealth.data.local.db.DatabaseProvider
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
import com.example.barnyhealth.data.repository.PetRepository
import com.example.barnyhealth.domain.AddMeasurementByMetricCodeUseCase
import com.example.barnyhealth.domain.DeleteMeasurementByMetricCodeUseCase
import com.example.barnyhealth.domain.GetActiveMetricTypesUseCase
import com.example.barnyhealth.domain.GetReferenceRangeForActivePetUseCase
import com.example.barnyhealth.domain.GetWeightHistoryUseCase
import com.example.barnyhealth.domain.ObserveMeasurementsByMetricCodeUseCase
import com.example.barnyhealth.domain.bootstrap.EnsureDefaultPetUseCase

class AppContainer(context: Context) {

    private val db = DatabaseProvider.get(context)

    val settingsDataStore = SettingsDataStore(context)

    val petRepository = PetRepository(
        petDao = db.petDao(),
        settingsDataStore = settingsDataStore
    )

    val measurementRepository = MeasurementRepository(
        measurementDao = db.measurementDao(),
        metricTypeDao = db.metricTypeDao(),
        referenceRangeDao = db.referenceRangeDao()
    )

    val ensureDefaultPetUseCase = EnsureDefaultPetUseCase(
        speciesDao = db.speciesDao(),
        petRepository = petRepository,
        settingsDataStore = settingsDataStore
    )

    val getWeightHistoryUseCase = GetWeightHistoryUseCase(
        settingsDataStore = settingsDataStore,
        measurementRepository = measurementRepository
    )

    val getActiveMetricTypesUseCase = GetActiveMetricTypesUseCase(
        measurementRepository = measurementRepository
    )

    val getReferenceRangeForActivePetUseCase = GetReferenceRangeForActivePetUseCase(
        petRepository = petRepository,
        measurementRepository = measurementRepository
    )

    val addMeasurementByMetricCodeUseCase = AddMeasurementByMetricCodeUseCase(
        settingsDataStore = settingsDataStore,
        measurementRepository = measurementRepository
    )

    val observeMeasurementsByMetricCodeUseCase = ObserveMeasurementsByMetricCodeUseCase(
        settingsDataStore = settingsDataStore,
        measurementRepository = measurementRepository
    )

    val deleteMeasurementByMetricCodeUseCase = DeleteMeasurementByMetricCodeUseCase(
        settingsDataStore = settingsDataStore,
        measurementRepository = measurementRepository
    )
}