package com.tezcatli.clicclac.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.helpers.TimeHelpers
import com.tezcatli.clicclac.settings.SettingsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class ConfigCassetteViewModel(
    private val settingsRepository: SettingsRepository
    ) : ViewModel() {

    var cassetteDevelopmentDelayChange by mutableStateOf("")
    var cassetteDevelopmentDelayValid by mutableStateOf(false)

    var shotsPerDaysChange by mutableStateOf("")
    var shotsPerDaysValid by mutableStateOf(false)

    var formValid by mutableStateOf(false)

    fun validateDevelopmentDelay(deadline : String) {
        TimeHelpers.stringToDuration(deadline).also {
            cassetteDevelopmentDelayValid = (it != 0.hours)
            cassetteDevelopmentDelayChange = deadline
        }
        validateForm()
    }

    fun validateShotsPerDays(change : String) {
        TimeHelpers.stringToDuration(change).also {
            shotsPerDaysValid = change.toIntOrNull() != null
            shotsPerDaysChange = change
        }
        validateForm()
    }

    fun validateForm() {
        formValid = cassetteDevelopmentDelayValid && shotsPerDaysValid
    }

    fun submitForm() {
        if (formValid) {
            viewModelScope.launch {
                settingsRepository.setCassetteDevelopmentDelay(cassetteDevelopmentDelayChange)
                settingsRepository.setShotsPerDays(shotsPerDaysChange.toInt())
            }
        }
    }
    init {
        viewModelScope.launch {
            cassetteDevelopmentDelayChange =
                    settingsRepository.getCassetteDevelopmentDelayF().filterNotNull().first()
            cassetteDevelopmentDelayValid = TimeHelpers.stringToDuration(cassetteDevelopmentDelayChange) != 0.hours

            shotsPerDaysChange =
                settingsRepository.getShotsPerDaysF().filterNotNull().first().toString()
            shotsPerDaysValid = shotsPerDaysChange.toIntOrNull() != null

            validateForm()

        }
    }
}


