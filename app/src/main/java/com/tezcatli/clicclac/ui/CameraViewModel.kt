package com.tezcatli.clicclac.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
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
import java.time.ZonedDateTime
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.hours

class CameraViewModel(
    private val executor : Executor,
    private val escrowManager: EscrowManager,
    private val settingsRepository: SettingsRepository,
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

            imageCapture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("kilo", "Take photo error:", exception)
                        ostream.outputStream.close()
//                        onError(exception)
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        //outputOptions.outputStream.close()
                        //val savedUri = Uri.fromFile(photoFile)
                        ostream.outputStream.close()
                        Log.i("Picture", "Photo shoot")
                        //onImageCaptured(savedUri)
                    }
                })
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

