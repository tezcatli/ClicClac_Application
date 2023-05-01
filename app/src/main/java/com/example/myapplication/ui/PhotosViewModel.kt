package com.example.myapplication.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.EscrowDbEntry
import com.example.myapplication.EscrowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
class PhotosViewModel(
    private val escrowManager: EscrowManager,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private var expiredList: List<EscrowDbEntry> = listOf()
    // val imageList: MutableList<Uri> = mutableListOf()
    val bitmapList : MutableList<Bitmap> = mutableListOf()

    private suspend fun recoverPhoto(
        uuid: String
    ): Uri {
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

        return (uri)
    }


    init {
        viewModelScope.launch {
            viewModelScope.launch {
                expiredList = escrowManager.listExpiredF().filterNotNull().first()
            }.join()
            val truc = expiredList.asFlow().map{recoverPhoto(it.UUID)}.collect() {
                bitmapList.add(contentResolver.loadThumbnail(
                        it, Size(640, 480), null
                    )
                )
            }
//            recoverPhoto(expiredList.map { it.UUID }).collect().{ value -> value }
        }
    }

}