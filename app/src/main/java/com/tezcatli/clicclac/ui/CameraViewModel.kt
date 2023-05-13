package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.Camera.CameraManager
import com.tezcatli.clicclac.EscrowManager
import com.tezcatli.clicclac.PendingPhotoNotificationManager
import com.tezcatli.clicclac.helpers.TimeHelpers
import com.tezcatli.clicclac.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.concurrent.Executor
import kotlin.time.Duration.Companion.hours

class CameraViewModel(
    private val executor: Executor,
    private val escrowManager: EscrowManager,
    private val settingsRepository: SettingsRepository,
    private val cameraManager: CameraManager,
    private val pendingPhotoNotificationManager: PendingPhotoNotificationManager,
) : ViewModel() {


    private var cassetteDevelopmentDelay = 0.hours
    var isInitialized by mutableStateOf(false)


    var cameraSelector by mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
    var lensDirection by mutableStateOf(CameraSelector.LENS_FACING_BACK)
    //  var lens by mutableStateOf<CameraManager.Lens>()

    var listLens = mutableStateListOf<CameraManager.Lens>()

    var flashMode by mutableStateOf(ImageCapture.FLASH_MODE_OFF)

    var shootCount by mutableStateOf(0)

    var isShutterOpen by mutableStateOf(true)



    fun takePhoto() {
        isShutterOpen = false
        cameraManager.takePhoto { imageCapture ->
            viewModelScope.launch(Dispatchers.IO) {


                val dateTime = ZonedDateTime.now()
                val uuid =
                    escrowManager.add(dateTime.plusNanos(cassetteDevelopmentDelay.inWholeNanoseconds))
                val ostream = escrowManager.EOutputStream(
                    uuid,
                    uuid,
                    dateTime.toLocalDateTime().toString() + ".jpg"
                ).build()
                val outputOptions =
                    ImageCapture.OutputFileOptions.Builder(ostream.outputStream).build()


                imageCapture.flashMode = flashMode

                imageCapture.takePicture(
                    outputOptions,
                    // executor,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CLICCLAC", "Take photo error:", exception)
                            try {
                                ostream.outputStream.close()
                            } catch (e: Exception) {
                                Log.e("CLICCLAC", "Caught exception $e")
                            } finally {
                                isShutterOpen = true
                            }
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            try {
                                ostream.outputStream.close()
                                Log.e("CLICLAC", "Photo shoot ")

                                pendingPhotoNotificationManager.scheduleNextNotification(true)
                            } catch (e: Exception) {
                                Log.e("CLICCLAC", "Caught exception $e")
                            } finally {
                                isShutterOpen = true
                            }
                        }
                    })
            }
        }
    }

    fun setSurface(previewView: PreviewView) {
        cameraManager.setSurface(previewView)
    }

    fun bind(lifecycleOwner: LifecycleOwner) {
        cameraManager.bind(lifecycleOwner, cameraSelector)
    }


    fun listLens(): List<CameraManager.Lens> {
        return cameraManager.listLens()
    }

    fun setLens(lens2: CameraSelector) {
        cameraSelector = lens2
    }

    fun updateLensDirection(lensDirection2: Int) {
        Log.e("CLICCLAC", "Switching from " + lensDirection + " to " + lensDirection2)
        if (lensDirection2 != lensDirection) {
            lensDirection = lensDirection2
            listLens.clear()
            cameraManager.listLens().forEach {
                if (it.direction == lensDirection) {
                    listLens.add(it)
                }
            }
            if (lensDirection2 == CameraSelector.LENS_FACING_BACK) {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }
        Log.e("CLICCLAC", "Switched to " + lensDirection)
    }

    fun updateFlashMode(flashMode: Int) {
        this.flashMode = flashMode
    }

    init {
        viewModelScope.launch {
            cassetteDevelopmentDelay = TimeHelpers.stringToDuration(
                settingsRepository.getCassetteDevelopmentDelayF().filterNotNull().first()
            )
        }

        cameraManager.waitForInitialization {
            listLens.clear()
            cameraManager.listLens().forEach {
                if (it.direction == lensDirection) {
                    listLens.add(it)
                }


                isInitialized = true
            }
        }
    }
}


