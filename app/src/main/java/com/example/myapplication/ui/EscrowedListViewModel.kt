package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel


import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.CliClacApplication
import com.example.myapplication.EscrowDbEntry
import com.example.myapplication.EscrowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

import java.time.ZonedDateTime

data class EscrowedState(
    val uuid: String = "",
    val deadline: ZonedDateTime = ZonedDateTime.parse("1980-01-01T00:00:00+00:00")
)

fun EscrowedStateFromDb(db: EscrowDbEntry): EscrowedState {
    return EscrowedState(uuid = db.UUID, deadline = db.deadline)
}

data class EscrowedListState(val itemList: List<EscrowedState> = listOf())


class EscrowedListViewModel(
    private val escrowManager: EscrowManager,
    private val appContext : Context
) : ViewModel() {

    //val truc = escrowManager.listPendingF().map { EscrowedListState(it.mapNotNull { EscrowedStateFromDb(it) }) }

    val pendingListState: StateFlow<EscrowedListState> = escrowManager.listPendingF()
        .map { EscrowedListState(it.mapNotNull { EscrowedStateFromDb(it) }) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EscrowedListState()
    )

    val expiredListState: StateFlow<EscrowedListState> = escrowManager.listExpiredF()
        .map { EscrowedListState(it.mapNotNull { EscrowedStateFromDb(it) }) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EscrowedListState()
    )

    fun recoverPhoto(
        uuid : String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {


            /*
            val ostream = escrowManager.EOutputStream(uuid, uuid, "$dateTime.jpg").build()


            val outputOptions = ImageCapture.OutputFileOptions.Builder(ostream.outputStream).build() */

            val istream = escrowManager.EInputStream(uuid, uuid).build()

            Log.e("ZOGZOG", "Stream Name = " + istream.streamName)

            //val file = File(appContext.filesDir, istream.streamName)
            val file = File(appContext.filesDir, "$uuid.jpg")
            Files.copy(istream.inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)

            escrowManager.delete(uuid)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L


        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val escrowManager = (this[APPLICATION_KEY] as CliClacApplication).escrowManager
                val appContext = (this[APPLICATION_KEY] as CliClacApplication).applicationContext
                EscrowedListViewModel(
                    escrowManager = escrowManager,
                    appContext = appContext,
                )
            }
        }
    }
}


