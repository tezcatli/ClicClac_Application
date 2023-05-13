package com.tezcatli.clicclac.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    fun getCassetteDevelopmentDelayF(): Flow<String> {
        return dataStore.data.map { preferences ->
            // No type safety.
            preferences[CASSETTE_DEVELOPMENT_DELAY] ?: "7 days"
        }
    }

    suspend fun setCassetteDevelopmentDelay(delay: String) {
        dataStore.edit { settings ->
            settings[CASSETTE_DEVELOPMENT_DELAY] = delay
        }
    }

    companion object Keys {
        val CASSETTE_DEVELOPMENT_DELAY = stringPreferencesKey("CASSETTE_DEVELOPMENT_DELAY")

        @Volatile
        private var Instance: SettingsRepository? = null

        fun getInstance(dataStore: DataStore<Preferences>): SettingsRepository {
            return Instance ?: synchronized(this) {
                return SettingsRepository(dataStore).also { Instance = it }
            }
        }

    }


}

