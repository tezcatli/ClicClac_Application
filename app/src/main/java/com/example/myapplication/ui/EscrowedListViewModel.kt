package com.example.myapplication.ui
import androidx.lifecycle.ViewModel


import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.CliClacApplication
import com.example.myapplication.EscrowDbEntry
import com.example.myapplication.EscrowManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import java.time.ZonedDateTime
import java.util.Date

data class EscrowedState (
    val UUID : String = "",
    val deadline : ZonedDateTime = ZonedDateTime.parse("1980-01-01T00:00:00+00:00")
    )

data class EscrowedListState(val itemList: List<EscrowDbEntry> = listOf())


class EscrowedListViewModel(
    private val escrowManager: EscrowManager
) : ViewModel() {

    val escrowedListState: StateFlow<EscrowedListState> = escrowManager.listPendingF().map { EscrowedListState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EscrowedListState()
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L


        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val escrowManager = (this[APPLICATION_KEY] as CliClacApplication).escrowManager
                EscrowedListViewModel(
                    escrowManager = escrowManager,
                )
            }
        }
    }
}


