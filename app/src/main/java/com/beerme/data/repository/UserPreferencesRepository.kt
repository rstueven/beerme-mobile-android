package com.beerme.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.beerme.data.model.BreweryStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        // All statuses visible until the user chooses otherwise.
        val DEFAULT_STATUS_FILTERS: Set<String> =
            BreweryStatus.entries.map { it.code }.toSet()
    }

    private object PreferencesKeys {
        val LAST_UPDATE_TIMESTAMP = stringPreferencesKey("last_update_timestamp")
        val BEER_LAST_UPDATE_TIMESTAMP = stringPreferencesKey("beer_last_update_timestamp")
        val STATUS_FILTERS = stringSetPreferencesKey("status_filters")
    }

    val lastUpdateTimestamp: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.LAST_UPDATE_TIMESTAMP]
        }

    val beerLastUpdateTimestamp: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.BEER_LAST_UPDATE_TIMESTAMP]
        }

    val statusFilters: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.STATUS_FILTERS] ?: DEFAULT_STATUS_FILTERS
        }

    suspend fun saveLastUpdateTimestamp(timestamp: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_UPDATE_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveBeerLastUpdateTimestamp(timestamp: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BEER_LAST_UPDATE_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveStatusFilters(filters: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STATUS_FILTERS] = filters
        }
    }
}
