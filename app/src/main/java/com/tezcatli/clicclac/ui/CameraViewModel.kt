package com.tezcatli.clicclac.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.R
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.EscrowManager
import com.tezcatli.clicclac.helpers.TimeHelpers
import com.tezcatli.clicclac.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.hours

class CameraViewModel(
    private val executor : Executor,
    private val escrowManager: EscrowManager,
    private val settingsRepository: SettingsRepository
//    private val appContext : Context
    ) : ViewModel() {

    private var cassetteDevelopmentDelay  = 0.hours



    fun takePhoto(

         imageCapture: ImageCapture,
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val dateTime = ZonedDateTime.now()
            val uuid =
                escrowManager.add(dateTime.plusNanos(cassetteDevelopmentDelay.inWholeNanoseconds))
            val ostream = escrowManager.EOutputStream(uuid, uuid, dateTime.toLocalDateTime().toString() + ".jpg").build()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(ostream.outputStream).build()


            imageCapture.let {imageCapture ->
                imageCapture.takePicture(
                    outputOptions,
                   // executor,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("kilo", "Take photo error:", exception)
                            ostream.outputStream.close()
//                        onError(exception)
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {


                            ostream.outputStream.close()
                            Log.i("Picture", "Photo shoot ")
                        }
                    })
            }
        }
    }

    init {
        viewModelScope.launch {
            cassetteDevelopmentDelay = TimeHelpers.stringToDuration(settingsRepository.getCassetteDevelopmentDelayF().filterNotNull().first())
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, executor)
    }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

