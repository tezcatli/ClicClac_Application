package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.CliClacApplication
import com.example.myapplication.EscrowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraViewModel(private val executor : Executor, private val escrowManager: EscrowManager) : ViewModel() {


    fun takePhoto(
        imageCapture: ImageCapture,
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val dateTime = ZonedDateTime.now()

            val uuid =
                escrowManager.add(dateTime.plusMinutes(2))

            val ostream = escrowManager.EOutputStream(uuid, uuid, dateTime.toLocalDateTime().toString() + ".jpg").build()


            val outputOptions = ImageCapture.OutputFileOptions.Builder(ostream.outputStream).build()

            imageCapture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {

                    val ostream = ostream

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("kilo", "Take photo error:", exception)
                        ostream.outputStream.close()
                        onError(exception)
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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val escrowManager = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CliClacApplication).escrowManager
                val executor = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CliClacApplication).mainExecutor
                CameraViewModel(
                    executor = executor,
                    escrowManager = escrowManager,
                )
            }
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
