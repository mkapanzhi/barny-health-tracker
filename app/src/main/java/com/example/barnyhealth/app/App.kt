package com.example.barnyhealth.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.barnyhealth.data.local.db.DatabaseProvider
import com.example.barnyhealth.data.local.db.DatabaseSeeder

class App : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)

        val db = DatabaseProvider.get(this)

        CoroutineScope(Dispatchers.IO).launch {
            DatabaseSeeder(db).seed()
            appContainer.ensureDefaultPetUseCase()
        }
    }
}