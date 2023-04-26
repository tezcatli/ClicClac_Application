package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine



@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(factory = CameraViewModel.Factory),
    onConfig: () -> Unit
//    onCapture: (ImageCapture) -> Unit,
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
                    viewModel.takePhoto(imageCapture)
//                    onCapture(imageCapture)

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
                    Log.i("kilo", "CONFIG")
                    onConfig()
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Details,
                        contentDescription = "Config",
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