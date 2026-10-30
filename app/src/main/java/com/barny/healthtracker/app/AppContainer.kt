package com.barny.healthtracker.app

import android.content.Context
import com.barny.healthtracker.DataRepository
import com.barny.healthtracker.LegacyMetricMetadataProvider
import com.barny.healthtracker.data.local.db.DatabaseProvider
import com.barny.healthtracker.data.preferences.SettingsDataStore
import com.barny.healthtracker.data.repository.MeasurementRepository
import com.barny.healthtracker.data.repository.PetRepository
import com.barny.healthtracker.domain.AddMeasurementByMetricCodeUseCase
import com.barny.healthtracker.domain.DeleteMeasurementByMetricCodeForDayUseCase
import com.barny.healthtracker.domain.DeleteMeasurementByMetricCodeUseCase
import com.barny.healthtracker.domain.GetActiveMetricTypesUseCase
import com.barny.healthtracker.domain.GetHomeMetricsUseCase
import com.barny.healthtracker.domain.GetReferenceRangeForActivePetUseCase
import com.barny.healthtracker.domain.GetWeightHistoryUseCase
import com.barny.healthtracker.domain.ObserveMeasurementsByMetricCodeUseCase
import com.barny.healthtracker.domain.bootstrap.EnsureDefaultPetUseCase
import com.barny.healthtracker.domain.ImportLegacyMeasurementsUseCase
import com.barny.healthtracker.domain.SaveMetricMeasurementUseCase
import com.barny.healthtracker.domain.CleanupMigratedLegacyMeasurementsUseCase


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