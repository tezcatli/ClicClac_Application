package com.example.myapplication.ui


import android.content.ContentResolver
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.EscrowDbEntry
import com.example.myapplication.EscrowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val listAllState: StateFlow<List<EscrowedState>> = escrowManager.listAllF()
        .map { it.map { EscrowedStateFromDb(it) } }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun recoverPhoto(
        uuid : String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val istream = escrowManager.EInputStream(uuid, uuid).build()

            val pictureDetails = ContentValues().apply {

                put(MediaStore.Images.Media.DISPLAY_NAME, istream.streamName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM)

            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                pictureDetails
            )

            val imageOutStream = contentResolver.openOutputStream(uri!!)

            val buf = ByteArray(8192)
            var length: Int
            while (istream.inputStream.read(buf).also { length = it } > 0) {
                imageOutStream?.write(buf, 0, length)
            }

            imageOutStream?.close()
            istream.inputStream.close()
            escrowManager.delete(uuid)
            escrowManager.deleteFile(uuid)
        }
    }

    private fun countExpired(list:  List<EscrowedState>) : Int {
        return list.sumOf {
            val duration = Duration.between(ZonedDateTime.now().toInstant(), it.deadline.toInstant())
            if (duration.isNegative ) {
                1.toInt()
            } else {
                0.toInt()
            }
        }
    }

    init {
        viewModelScope.launch {
            listAllState.collect {  list ->
                expiredNumber = countExpired(list)
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

    }
}


