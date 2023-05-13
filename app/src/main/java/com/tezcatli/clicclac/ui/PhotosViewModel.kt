package com.tezcatli.clicclac.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toUri
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
import java.io.File
import java.nio.file.StandardCopyOption


class PhotosViewModel(
    private val escrowManager: EscrowManager,
    val contentResolver: ContentResolver
) : ViewModel() {

    private var expiredList: List<EscrowDbEntry> = listOf()

    val imageUriList = mutableStateListOf<Uri>()

    private suspend fun recoverPhoto(
        uuid: String
    ): Uri? {

        var uri: Uri?
        try {
            val istream = escrowManager.EInputStream(uuid, uuid).build()

            istream.inputStream.use {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val pictureDetails = ContentValues().apply {

                        put(MediaStore.Images.Media.DISPLAY_NAME, istream.streamName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_DCIM + "/ClicClac"
                        )

                    }

                    val pictureCollection =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            MediaStore.Images.Media.getContentUri(
                                MediaStore.VOLUME_EXTERNAL_PRIMARY
                            )
                        } else {
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }

                    Log.e("CLICCLAC", "pictureCollecion =  " + pictureCollection.toString())

                    uri = contentResolver.insert(
                        //                 MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        pictureCollection,
                        pictureDetails
                    )

                    Log.e("CLICCLAC", "uri =  " + uri.toString())

                    contentResolver.openOutputStream(uri!!).use {
                        val buf = ByteArray(8192)
                        var length: Int
                        while (istream.inputStream.read(buf).also { length = it } > 0) {
                            it?.write(buf, 0, length)
                        }
                    }
                } else {
                    val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString(), "/ClicClac" + istream.streamName)

                    java.nio.file.Files.copy(
                        istream.inputStream,
                        outputFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING)
                    uri = outputFile.toUri()
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