package com.barny.healthtracker.app

import android.app.Application
import android.util.Log
import com.barny.healthtracker.HealthParams
import com.barny.healthtracker.PetSpecies
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
        HealthParams.selectSpecies(PetSpecies.fromStorage(
            getSharedPreferences("onboarding_prefs", MODE_PRIVATE).getString("pet_species", null)
        ))
        appContainer = AppContainer(this)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                DatabaseSeeder(DatabaseProvider.get(this@App)).seed()
                appContainer.importLegacyMeasurementsUseCase()
            }.onFailure { Log.e("AppStartup", "Startup initialization failed", it) }
        }
    }
}
