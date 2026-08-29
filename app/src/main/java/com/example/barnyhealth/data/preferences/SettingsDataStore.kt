package com.example.barnyhealth.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val SELECTED_HOME_METRIC_KEY = stringPreferencesKey("selected_home_metric_key")

        // Pet profile
        val PET_NAME = stringPreferencesKey("pet_name")
        val PET_PHOTO_URI = stringPreferencesKey("pet_photo_uri")
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    val activePetIdFlow: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.ACTIVE_PET_ID] }

    val legacyImportDoneFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.LEGACY_IMPORT_DONE] ?: false }

    val legacyCleanupDoneFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.LEGACY_CLEANUP_DONE] ?: false }

    val selectedHomeMetricKeyFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.SELECTED_HOME_METRIC_KEY] }

    // Pet profile flows
    val petNameFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.PET_NAME].orEmpty() }

    val petPhotoUriFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[Keys.PET_PHOTO_URI].orEmpty() }

    suspend fun setSelectedHomeMetricKey(value: String?) {
        context.dataStore.edit { prefs ->
            if (value == null) {
                prefs.remove(Keys.SELECTED_HOME_METRIC_KEY)
            } else {
                prefs[Keys.SELECTED_HOME_METRIC_KEY] = value
            }
        }
    }

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

    // Pet profile methods
    suspend fun setPetName(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PET_NAME] = value
        }
    }

    suspend fun setPetPhotoUri(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PET_PHOTO_URI] = value
        }
    }
}