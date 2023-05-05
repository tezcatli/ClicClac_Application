package com.tezcatli.clicclac.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.EscrowDbEntry
import com.tezcatli.clicclac.EscrowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class PhotosViewModel(
    private val escrowManager: EscrowManager,
    val contentResolver: ContentResolver
) : ViewModel() {

    private var expiredList: List<EscrowDbEntry> = listOf()
    // val imageList: MutableList<Uri> = mutableListOf()
    //val bitmapList : MutableList<Bitmap> =  mutableListOf()

    val imageBitmapList = mutableStateListOf<ImageBitmap>()

    val imageUriList = mutableStateListOf<Uri>()

    private suspend fun recoverPhoto(
        uuid: String
    ): Uri? {

        var uri: Uri?
        try {
            val istream = escrowManager.EInputStream(uuid, uuid).build()

            istream.inputStream.use {

                val pictureDetails = ContentValues().apply {

                    put(MediaStore.Images.Media.DISPLAY_NAME, istream.streamName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM)

                }

                uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    pictureDetails
                )

                contentResolver.openOutputStream(uri!!).use {
                    val buf = ByteArray(8192)
                    var length: Int
                    while (istream.inputStream.read(buf).also { length = it } > 0) {
                        it?.write(buf, 0, length)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("PHOTOSCREEN", "Exception caught:", e)
            return null
        } finally {
            escrowManager.delete(uuid)
            escrowManager.deleteFile(uuid)
        }
        return (uri)
    }


    init {
        viewModelScope.launch {
            expiredList = escrowManager.listExpiredF().filterNotNull().first()
            expiredList.asFlow().map { recoverPhoto(it.UUID) }.flowOn(Dispatchers.IO).collect() {

                if (it != null) {
                    Log.d("CLICLAC", "Loading bitmap at URL $it")

                    imageUriList.add(it)

                    /*
                    val source = ImageDecoder.createSource(contentResolver, it)

                    try {
                        imageBitmapList.add(ImageDecoder.decodeBitmap(source).asImageBitmap())
                    } catch (e: Exception) {
                        Log.e("CLICLAC", "Exception caught wile decoding image: ", e)
                    }
                    */
                }
            }
        }
    }
}