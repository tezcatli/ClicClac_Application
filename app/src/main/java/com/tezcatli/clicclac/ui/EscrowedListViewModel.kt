package com.tezcatli.clicclac.ui


import android.content.ContentResolver
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.EscrowDbEntry
import com.tezcatli.clicclac.EscrowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
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
    private val contentResolver: ContentResolver
) : ViewModel() {

    //val truc = escrowManager.listPendingF().map { EscrowedListState(it.mapNotNull { EscrowedStateFromDb(it) }) }

    var expiredNumber by mutableStateOf(0)

    val pendingListState: StateFlow<EscrowedListState> = escrowManager.listPendingF()
        .map { EscrowedListState(it.map { EscrowedStateFromDb(it) }) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EscrowedListState()
    )

    val expiredListState: StateFlow<EscrowedListState> = escrowManager.listExpiredF()
        .map { EscrowedListState(it.map { EscrowedStateFromDb(it) }) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = EscrowedListState()
    )

    val listAllState: StateFlow<List<EscrowedState>> = escrowManager.listAllF()
        .map { it.map { EscrowedStateFromDb(it) } }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    private fun countExpired(list:  List<EscrowedState>) : Int {
        return list.sumOf {
            val duration = Duration.between(ZonedDateTime.now().toInstant(), it.deadline.toInstant())
            if (duration.isNegative ) {
                1.toInt()
            } else {
                0
            }
        }
    }
/*
    data class Expired (
        val expiredNumber : Int,
        val nextExpiration : Int,
            )
            */
    init {
        viewModelScope.launch {
//            listAllState.collect {  list ->
//                expiredNumber = countExpired(list)
//           }
            while (true) {
                expiredNumber = countExpired(listAllState.filterNotNull().first())
                delay(1000)
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

    }
}


