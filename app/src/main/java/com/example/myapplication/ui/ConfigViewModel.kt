package com.example.myapplication.ui


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime


class ConfigViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val cassetteDevelopmentDelayState: StateFlow<String> = settingsRepository.getCassetteDevelopmentDelayF().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(ConfigViewModel.TIMEOUT_MILLIS),
        initialValue = ""
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

    }
}


