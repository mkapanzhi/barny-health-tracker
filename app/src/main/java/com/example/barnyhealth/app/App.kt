package com.example.barnyhealth.app

import android.app.Application
import com.example.barnyhealth.data.local.db.DatabaseProvider
import com.example.barnyhealth.data.local.db.DatabaseSeeder
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
                appContainer.cleanupMigratedLegacyMeasurementsUseCase()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}