package com.example.barnyhealth.app

import android.content.Context
import com.example.barnyhealth.DataRepository
import com.example.barnyhealth.LegacyMetricMetadataProvider
import com.example.barnyhealth.data.local.db.DatabaseProvider
import com.example.barnyhealth.data.preferences.SettingsDataStore
import com.example.barnyhealth.data.repository.MeasurementRepository
import com.example.barnyhealth.data.repository.PetRepository
import com.example.barnyhealth.domain.AddMeasurementByMetricCodeUseCase
import com.example.barnyhealth.domain.DeleteMeasurementByMetricCodeForDayUseCase
import com.example.barnyhealth.domain.DeleteMeasurementByMetricCodeUseCase
import com.example.barnyhealth.domain.GetActiveMetricTypesUseCase
import com.example.barnyhealth.domain.GetHomeMetricsUseCase
import com.example.barnyhealth.domain.GetReferenceRangeForActivePetUseCase
import com.example.barnyhealth.domain.GetWeightHistoryUseCase
import com.example.barnyhealth.domain.ObserveMeasurementsByMetricCodeUseCase
import com.example.barnyhealth.domain.bootstrap.EnsureDefaultPetUseCase
import com.example.barnyhealth.domain.ImportLegacyMeasurementsUseCase
import com.example.barnyhealth.domain.SaveMetricMeasurementUseCase
import com.example.barnyhealth.domain.CleanupMigratedLegacyMeasurementsUseCase


class AppContainer(context: Context) {

    private val db = DatabaseProvider.get(context)

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context)
    }

    val dataRepository: DataRepository by lazy {
        DataRepository(context)
    }

    private val legacyMetricMetadataProvider: LegacyMetricMetadataProvider by lazy {
        LegacyMetricMetadataProvider()
    }

    val petRepository: PetRepository by lazy {
        PetRepository(
            petDao = db.petDao(),
            settingsDataStore = settingsDataStore
        )
    }

    val measurementRepository: MeasurementRepository by lazy {
        MeasurementRepository(
            measurementDao = db.measurementDao(),
            metricTypeDao = db.metricTypeDao(),
            referenceRangeDao = db.referenceRangeDao()
        )
    }

    val ensureDefaultPetUseCase: EnsureDefaultPetUseCase by lazy {
        EnsureDefaultPetUseCase(
            speciesDao = db.speciesDao(),
            petRepository = petRepository,
            settingsDataStore = settingsDataStore
        )
    }

    val addMeasurementByMetricCodeUseCase: AddMeasurementByMetricCodeUseCase by lazy {
        AddMeasurementByMetricCodeUseCase(
            settingsDataStore = settingsDataStore,
            measurementRepository = measurementRepository
        )
    }

    val importLegacyMeasurementsUseCase: ImportLegacyMeasurementsUseCase by lazy {
        ImportLegacyMeasurementsUseCase(
            settingsDataStore = settingsDataStore,
            measurementRepository = measurementRepository,
            dataRepository = dataRepository
        )
    }

    val getWeightHistoryUseCase: GetWeightHistoryUseCase by lazy {
        GetWeightHistoryUseCase(
            settingsDataStore = settingsDataStore,
            measurementRepository = measurementRepository
        )
    }

    val getActiveMetricTypesUseCase: GetActiveMetricTypesUseCase by lazy {
        GetActiveMetricTypesUseCase(
            measurementRepository = measurementRepository
        )
    }

    val getReferenceRangeForActivePetUseCase: GetReferenceRangeForActivePetUseCase by lazy {
        GetReferenceRangeForActivePetUseCase(
            petRepository = petRepository,
            measurementRepository = measurementRepository
        )
    }

    val observeMeasurementsByMetricCodeUseCase: ObserveMeasurementsByMetricCodeUseCase by lazy {
        ObserveMeasurementsByMetricCodeUseCase(
            settingsDataStore = settingsDataStore,
            measurementRepository = measurementRepository
        )
    }

    val deleteMeasurementByMetricCodeUseCase: DeleteMeasurementByMetricCodeUseCase by lazy {
        DeleteMeasurementByMetricCodeUseCase(
            settingsDataStore = settingsDataStore,
            measurementRepository = measurementRepository
        )
    }

    val getHomeMetricsUseCase: GetHomeMetricsUseCase by lazy {
        GetHomeMetricsUseCase(
            petRepository = petRepository,
            measurementRepository = measurementRepository,
            legacyMetricMetadataProvider = legacyMetricMetadataProvider
        )
    }

    val deleteMeasurementByMetricCodeForDayUseCase: DeleteMeasurementByMetricCodeForDayUseCase by lazy {
        DeleteMeasurementByMetricCodeForDayUseCase(
            settingsDataStore = settingsDataStore,
            measurementRepository = measurementRepository
        )
    }

    val saveMetricMeasurementUseCase: SaveMetricMeasurementUseCase by lazy {
        SaveMetricMeasurementUseCase(
            dataRepository = dataRepository,
            addMeasurementByMetricCodeUseCase = addMeasurementByMetricCodeUseCase
        )
    }

    val cleanupMigratedLegacyMeasurementsUseCase: CleanupMigratedLegacyMeasurementsUseCase by lazy {
        CleanupMigratedLegacyMeasurementsUseCase(
            settingsDataStore = settingsDataStore,
            dataRepository = dataRepository
        )
    }
}