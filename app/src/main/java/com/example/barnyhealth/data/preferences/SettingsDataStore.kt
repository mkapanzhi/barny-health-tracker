package com.example.barnyhealth.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(
    private val context: Context
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_PET_ID = longPreferencesKey("active_pet_id")
        val LEGACY_IMPORT_DONE = booleanPreferencesKey("legacy_import_done")
        val LEGACY_CLEANUP_DONE = booleanPreferencesKey("legacy_cleanup_done")
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    val activePetIdFlow: Flow<Long?> = context.dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs -> prefs[Keys.ACTIVE_PET_ID] }

    val legacyImportDoneFlow: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs -> prefs[Keys.LEGACY_IMPORT_DONE] ?: false }

    val legacyCleanupDoneFlow: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { prefs -> prefs[Keys.LEGACY_CLEANUP_DONE] ?: false }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = value
        }
    }

    suspend fun setActivePetId(value: Long?) {
        context.dataStore.edit { prefs ->
            if (value == null) {
                prefs.remove(Keys.ACTIVE_PET_ID)
            } else {
                prefs[Keys.ACTIVE_PET_ID] = value
            }
        }
    }

    suspend fun setLegacyImportDone(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LEGACY_IMPORT_DONE] = value
        }
    }

    suspend fun setLegacyCleanupDone(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LEGACY_CLEANUP_DONE] = value
        }
    }
}