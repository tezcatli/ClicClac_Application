package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Camera
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import kotlinx.coroutines.launch


@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onConfig: () -> Unit = {}
) {

    CameraScreen2(
        onConfig = onConfig,
        onCapture = viewModel::takePhoto
    )
}

@Composable
fun CameraScreen2(
//    viewModel: CameraViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onConfig: () -> Unit = {},
//    onTakePicture : (ImageCapture) -> Unit = {}
    onCapture: (ImageCapture) -> Unit = {}
) {
    //val escrowedListState by viewModel.escrowedListState.collectAsState()

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


    val scale = remember {  Animatable(1f)  }

    val scope = rememberCoroutineScope()

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

    /*
    DisposableEffect(lifecycleOwner) {

        onDispose {
        }
    }
    */

    // 3
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize().graphicsLayer(alpha = scale.value))
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
//                    viewModel.takePhoto(imageCapture)
                    scope.launch {
                        scale.animateTo(
                            0f,
                            animationSpec = tween(250),
                        )
                        scale.animateTo(
                            1f,
                            animationSpec = tween(250),
                        )
                    }
                    onCapture(imageCapture)
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Camera,
                        contentDescription = "Take picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                    )
                }
            )


            IconButton(
                modifier = Modifier.weight(1.0f),
                onClick = {
                    Log.i("kilo", "CONFIG")
                    onConfig()
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Menu,
                        contentDescription = "Config",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                    )
                }
            )
        }
    }

}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraScreenPreview () {
    CameraScreen2()
}