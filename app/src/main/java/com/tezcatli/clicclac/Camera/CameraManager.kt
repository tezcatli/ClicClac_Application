package com.tezcatli.clicclac.Camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(@ApplicationContext val context : Context) {

    data class Lens(val zoom : Float, val direction : Int, val selector : CameraSelector)
    val lensList : MutableList<Lens> = mutableListOf()

    var isInitialized = false

    var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    var preview : Preview? = null

    //var cameraSelected = CameraSelector.DEFAULT_BACK_CAMERA

    lateinit var camera : Camera
    lateinit var processCameraProvider : ProcessCameraProvider
    var imageCapture: ImageCapture ? = null
    //val preview = Preview.Builder().build()


    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    var truc : CameraInfo? = null



    init {
        cameraProviderFuture.addListener({
            processCameraProvider = cameraProviderFuture.get()
            processCameraProvider.unbindAll()

            processCameraProvider.availableCameraInfos.forEachIndexed() { index, it ->
                lensList.add(Lens(it.intrinsicZoomRatio, it.lensFacing, it.cameraSelector))
            }

            isInitialized = true

        }, context.mainExecutor)

    }

    fun listLens() : List<Lens>  {
        return lensList
    }

    fun waitForInitialization(onReady : ()->Unit ) {
        cameraProviderFuture.addListener( {
            preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()
            onReady()
        }, context.mainExecutor)
    }

    fun bind(lifecycleOwner : LifecycleOwner, cameraSelector : CameraSelector) {
        unbindAll()
        camera = processCameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
    }

    fun unbindAll() {
        processCameraProvider.unbindAll()
    }

    fun setSurface(previewView : PreviewView) {
        preview?.setSurfaceProvider(previewView.surfaceProvider)
    }

    fun takePhoto(process: (ImageCapture) -> Unit) {
        process(imageCapture!!)
    }

}