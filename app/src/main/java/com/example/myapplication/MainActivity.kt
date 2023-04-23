package com.example.myapplication

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Details
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview as CPreview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario.launch
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class MainActivity : ComponentActivity(), CoroutineScope {
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var escrowManager: EscrowManager

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)

    private lateinit var listPending: List<EscrowDbEntry>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mJob = Job()

        launch(Dispatchers.IO) {

            //  async(Dispatchers.IO) {
            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()

            escrowManager = EscrowManager(applicationContext)


            escrowManager.init(URL("http://10.0.2.2:5000"))

            listPending = listOf<EscrowDbEntry>()



            requestCameraPermission()



            //  EscrowedUIi(pending = listPending)

            //    }
            //}
        }
        setContent {

            CameraView(
                onCapture = ::takePhoto,
            )
        }
        //requestCameraPermission()


    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
        } else {
            Log.i("kilo", "Permission denied")
        }
    }

    private fun handleImageCapture(uri: Uri) {
        Log.i("kilo", "Image captured: $uri")
        shouldShowCamera.value = false
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto(
        imageCapture: ImageCapture,
    ) {
        launch(Dispatchers.IO) {

            val dateTime = ZonedDateTime.now()

            val uuid =
                escrowManager.add(dateTime.plusMinutes(10))

            val ostream = escrowManager.EOutputStream(uuid, uuid, "$dateTime.jpg")


            val outputOptions = ImageCapture.OutputFileOptions.Builder(ostream.outputStream).build()

            imageCapture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e("kilo", "Take photo error:", exception)
                        onError(exception)
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        //val savedUri = Uri.fromFile(photoFile)
                        Log.i("Picture", "Photo shoot")
                        //onImageCaptured(savedUri)
                    }
                })
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


@Composable
fun CameraView(
    onCapture: (ImageCapture) -> Unit,
) {
    // 1
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }

    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // 3
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        Row(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(Modifier.weight(1.0f))

            IconButton(
                modifier = Modifier.weight(1.0f),
                onClick = {
                    Log.i("kilo", "ON CLICK")
                    onCapture(imageCapture)
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Lens,
                        contentDescription = "Take picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            )


            IconButton(
                modifier = Modifier.weight(1.0f),
                onClick = {
                    Log.i("kilo", "ON CLICK")
                    onCapture(imageCapture)
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Details,
                        contentDescription = "Take picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            )
        }
    }
}


@CPreview
@Composable
fun DefaultPreview() {
    CameraView(
        onCapture = {}
    )
}
