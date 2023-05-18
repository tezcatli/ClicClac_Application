package com.tezcatli.clicclac.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class ConfigViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val cassetteDevelopmentDelayState: StateFlow<Long> =
        settingsRepository.getCassetteDevelopmentDelayF().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = 0
        )

    val shotsPerDays: StateFlow<Int> =
        settingsRepository.getShotsPerDaysF().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = 10
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

    }
}


