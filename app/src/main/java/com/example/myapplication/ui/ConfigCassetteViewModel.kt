package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.helpers.TimeHelpers
import com.example.myapplication.settings.SettingsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class ConfigCassetteViewModel(
    private val settingsRepository: SettingsRepository
    ) : ViewModel() {

    var cassetteDevelopmentDelayChange by mutableStateOf("")
    var cassetteDevelopmentDelayValid by mutableStateOf(false)


    fun validateDevelopmentDelay(deadline : String) {
        TimeHelpers.stringToDuration(deadline).also {
            cassetteDevelopmentDelayValid = (it != 0.hours)
            cassetteDevelopmentDelayChange = deadline
        }
    }

    fun setDevelopmentDelay() {
        if (cassetteDevelopmentDelayValid) {
            viewModelScope.launch {
                settingsRepository.setCassetteDevelopmentDelay(cassetteDevelopmentDelayChange)
            }
        }
    }

    init {
        viewModelScope.launch {
            viewModelScope.launch {
                cassetteDevelopmentDelayChange =
                    settingsRepository.getCassetteDevelopmentDelayF().filterNotNull().first()
            }.join()
            cassetteDevelopmentDelayValid = TimeHelpers.stringToDuration(cassetteDevelopmentDelayChange) != 0.hours
        }
    }
}


