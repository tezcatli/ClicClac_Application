package com.tezcatli.clicclac.ui


import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class ConfigViewModel(
    private val settingsRepository: SettingsRepository,
    private val packageManager: PackageManager
) : ViewModel() {

    val cassetteDevelopmentDelayState: StateFlow<String> =
        settingsRepository.getCassetteDevelopmentDelayF().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ""
        )

  //  val versionName = packageManager.

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

    }
}


