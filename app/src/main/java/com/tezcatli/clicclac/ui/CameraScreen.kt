package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.sharp.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.tezcatli.clicclac.Camera.CameraManager


fun flashModeIcon(flashMode: Int): ImageVector {
    return when (flashMode) {
        ImageCapture.FLASH_MODE_OFF -> Icons.Outlined.FlashOff
        ImageCapture.FLASH_MODE_AUTO -> Icons.Outlined.FlashAuto
        ImageCapture.FLASH_MODE_ON -> Icons.Outlined.FlashOn
        else -> Icons.Outlined.Error
    }
}

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onConfig: () -> Unit = {}
) {

    CameraScreen2(
        isInitialized = viewModel.isInitialized,
        listLens = viewModel.listLens,
        cameraSelector = viewModel.cameraSelector,
        lensDirection = viewModel.lensDirection,
        setLens = viewModel::setLens,
        updateLensDirection = viewModel::updateLensDirection,
        flashMode = viewModel.flashMode,
        updateFlashMode = viewModel::updateFlashMode,
        isShutterOpen = viewModel.isShutterOpen,
        shotsRemaining = viewModel.shotsRemaining,
        setSurface = viewModel::setSurface,
        bind = viewModel::bind,
        onConfig = onConfig,
        onCapture = viewModel::takePhoto
    )
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun CameraScreen2(
    isInitialized: Boolean = false,
    listLens: List<CameraManager.Lens> = listOf(),
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    lensDirection: Int = CameraSelector.LENS_FACING_BACK,
    setLens: (CameraSelector) -> Unit = {},
    updateLensDirection: (Int) -> Unit = {},
    flashMode: Int = ImageCapture.FLASH_MODE_AUTO,
    updateFlashMode: (Int) -> Unit = {},
    isShutterOpen: Boolean = true,
    shotsRemaining: Int = 0,
    setSurface: (PreviewView) -> Unit = {},
    bind: (LifecycleOwner) -> Unit = {},
    onConfig: () -> Unit = {},
    onCapture: () -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    var flashlightMenuExpanded by remember { mutableStateOf(false) }


    if (isInitialized) {
        val previewView = remember { PreviewView(context) }

        val scale = remember { Animatable(1f) }

        LaunchedEffect(cameraSelector) {
            setSurface(previewView)
            bind(lifecycleOwner)
        }

        LaunchedEffect(isShutterOpen) {
            if (!isShutterOpen) {
                scale.animateTo(
                    0f,
                    animationSpec = tween(250),
                )
            } else {
                scale.animateTo(
                    1f,
                    animationSpec = tween(250),
                )
            }
        }

        // 3
        if (isInitialized) {

            Box(
                contentAlignment = Alignment.BottomCenter, modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    { previewView }, modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = scale.value)
                )

                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column()
                        {
                            IconButton(onClick = {
                                flashlightMenuExpanded = !flashlightMenuExpanded
                            }) {
                                Icon(
                                    imageVector = flashModeIcon(flashMode),
                                    tint = Color.White,
                                    contentDescription = "Localized description"
                                )
                            }

                            if (flashlightMenuExpanded) {

                                listOf(
                                    ImageCapture.FLASH_MODE_OFF,
                                    ImageCapture.FLASH_MODE_AUTO,
                                    ImageCapture.FLASH_MODE_ON
                                ).forEach {
                                    if (flashMode != it)

                                        IconButton(onClick = {
                                            updateFlashMode(it)
                                            flashlightMenuExpanded = false
                                        }) {
                                            Icon(
                                                imageVector = flashModeIcon(it),
                                                tint = Color.White,
                                                contentDescription = "Localized description"
                                            )
                                        }
                                }
                            }
                        }
                        Column(modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(end = 10.dp)) {
                            Box {
                                Text(
                                    shotsRemaining.toString(), fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = if (shotsRemaining > 0) Color.White else Color.Red,
                                )
                            }
                        }
                    }


                    Spacer(modifier = Modifier.weight(100.0f, true))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listLens.forEach {
                            Button(colors = if (it.selector == cameraSelector) {
                                buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            },
                                onClick = { setLens(it.selector) }) {
                                Text("%.2f".format(it.zoom))
                            }
                        }

                    }
                    Row(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = {
                                Log.e("CLICCLAC", "Current direction " + lensDirection)

                                if (lensDirection == CameraSelector.LENS_FACING_BACK) {
                                    Log.e("CLICCLAC", "1 Current direction " + lensDirection)
                                    updateLensDirection(CameraSelector.LENS_FACING_FRONT)
                                } else {
                                    Log.e("CLICCLAC", "2 Current direction " + lensDirection)
                                    updateLensDirection(CameraSelector.LENS_FACING_BACK)
                                }
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Cached,
                                    contentDescription = "Font / Back",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(1.dp)
                                )
                            }
                        )

                        IconButton(
                            onClick = {
                                if (isShutterOpen && shotsRemaining > 0) {
                                    onCapture()
                                }
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Sharp.Camera,
                                    contentDescription = "Take picture",
                                    tint = if (shotsRemaining > 0)
                                        Color.White else Color.Red,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(1.dp)
                                )
                            }
                        )


                        IconButton(
                            onClick = {
                                Log.i("kilo", "CONFIG")
                                onConfig()
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Menu,
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
        }
    }
}

/*
@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraScreenPreview() {
    CameraScreen2()
}
*/


