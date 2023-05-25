package com.tezcatli.clicclac.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Singleton
class SettingsRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    fun getCassetteDevelopmentDelayF(): Flow<Long> {
        return dataStore.data.map { preferences ->
            // No type safety.
            preferences[CASSETTE_DEVELOPMENT_DELAY] ?: 7L.days.inWholeSeconds
        }
    }

    suspend fun setCassetteDevelopmentDelay(delay: Duration) {
        dataStore.edit { settings ->
            settings[CASSETTE_DEVELOPMENT_DELAY] = delay.inWholeSeconds
        }
    }

    fun getShotsPerDaysF(): Flow<Int> {
        return dataStore.data.map { preferences ->
            // No type safety.
           (preferences[SHOTS_PER_DAYS] ?: 10)
        }
    }

    suspend fun setShotsPerDays(shotsPerDay: Int) {
        dataStore.edit { settings ->
            settings[SHOTS_PER_DAYS] = shotsPerDay
        }
    }

    fun getShotsInDayF(): Flow<Int> {
        return dataStore.data.map { preferences ->
            // No type safety.
            (preferences[SHOTS_IN_DAY] ?: 0)
        }
    }

    suspend fun setShotsInDay(shotsInDay: Int) {
        dataStore.edit { settings ->
            settings[SHOTS_IN_DAY] = shotsInDay
        }
    }

    fun getLastShotTimeStampF(): Flow<String> {
        return dataStore.data.map { preferences ->
            // No type safety.
            (preferences[LAST_SHOT_TIMESTAMP] ?: "1970-01-01T00:00:00+00:00")
        }
    }

    suspend fun setLastShotTimeStamp(timeStamp: String) {
        dataStore.edit { settings ->
            settings[LAST_SHOT_TIMESTAMP] = timeStamp
        }
    }

    companion object Keys {
        val CASSETTE_DEVELOPMENT_DELAY = longPreferencesKey("CASSETTE_DEVELOPMENT_DELAY")
        val SHOTS_PER_DAYS = intPreferencesKey("SHOTS_PER_DAYS")
        val SHOTS_IN_DAY = intPreferencesKey("SHOTS_IN_DAY")
        val LAST_SHOT_TIMESTAMP = stringPreferencesKey("LAST_SHOT_TIMESTAMP")


    }


}

