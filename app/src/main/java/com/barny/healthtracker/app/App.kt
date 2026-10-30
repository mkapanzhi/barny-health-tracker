package com.barny.healthtracker.app

import android.app.Application
import android.util.Log
import com.barny.healthtracker.data.local.db.DatabaseProvider
import com.barny.healthtracker.data.local.db.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)

        val db = DatabaseProvider.get(this)

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                DatabaseSeeder(db).seed()
                appContainer.ensureDefaultPetUseCase()
                appContainer.importLegacyMeasurementsUseCase()

                // Legacy cleanup is intentionally disabled.
                // The current legacy import is not atomic and cannot guarantee
                // that every Room-backed measurement was migrated before cleanup.
                // Re-enable only after introducing a transactional migration
                // with successful-import verification.
                // appContainer.cleanupMigratedLegacyMeasurementsUseCase()
            }.onFailure { error ->
                Log.e("AppStartup", "Startup initialization failed", error)
            }
        }
    }
}
