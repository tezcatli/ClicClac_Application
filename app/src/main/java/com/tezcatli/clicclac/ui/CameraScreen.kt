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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Cached
import androidx.compose.material.icons.sharp.Camera
import androidx.compose.material.icons.sharp.Error
import androidx.compose.material.icons.sharp.FlashAuto
import androidx.compose.material.icons.sharp.FlashOff
import androidx.compose.material.icons.sharp.FlashOn
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import com.tezcatli.clicclac.Camera.CameraManager
import kotlinx.coroutines.launch


private const val TAG = "CameraXBasic"
private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val PHOTO_TYPE = "image/jpeg"
private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0


// Create time stamped name and MediaStore entry.

fun flashModeIcon(flashMode : Int) : ImageVector {
    return when (flashMode)  {
        ImageCapture.FLASH_MODE_OFF -> Icons.Sharp.FlashOff
        ImageCapture.FLASH_MODE_AUTO -> Icons.Sharp.FlashAuto
        ImageCapture.FLASH_MODE_ON -> Icons.Sharp.FlashOn
        else -> Icons.Sharp.Error
    }
}
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(factory = AppViewModelProvider.Factory),
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
        setSurface = viewModel::setSurface,
        bind = viewModel::bind,
        onConfig = onConfig,
        onCapture = viewModel::takePhoto
    )
}


@Composable
fun CameraScreen2(
    isInitialized: Boolean = false,
    listLens: List<CameraManager.Lens> = listOf(),
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    lensDirection: Int = CameraSelector.LENS_FACING_BACK,
    setLens: (CameraSelector) -> Unit = {},
    updateLensDirection: (Int) -> Unit = {},
    flashMode : Int = ImageCapture.FLASH_MODE_OFF,
    updateFlashMode : (Int) -> Unit = {},
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
        val scope = rememberCoroutineScope()

        LaunchedEffect(cameraSelector) {
            setSurface(previewView)
            bind(lifecycleOwner)
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
                //          Box(
                //              contentAlignment = Alignment.TopCenter, modifier = Modifier
                //                  .fillMaxSize()
                //                  .background(Color.Transparent)
                //          ) {
                //              Text("TOTO")
                //          }
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)) {
                        IconButton(onClick = { flashlightMenuExpanded = true }) {
                            Icon(
                                imageVector = flashModeIcon(flashMode),
                                tint = Color.White,
                                contentDescription = "Localized description"
                            )
                        }

                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent, surfaceTint = Color.Transparent)
                        ) {
                            DropdownMenu(
                                modifier = Modifier.background(Color.Transparent),
                                expanded = flashlightMenuExpanded,
                                onDismissRequest = { flashlightMenuExpanded = false }
                            ) {
                                IconButton(onClick = {
                                    updateFlashMode(ImageCapture.FLASH_MODE_OFF)
                                    flashlightMenuExpanded = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Sharp.FlashOff,
                                        tint = Color.White,
                                        contentDescription = "Localized description"
                                    )
                                }
                                IconButton(onClick = {
                                    updateFlashMode(ImageCapture.FLASH_MODE_AUTO)
                                    flashlightMenuExpanded = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Sharp.FlashAuto,
                                        tint = Color.White,
                                        contentDescription = "Localized description"
                                    )
                                }
                                IconButton(onClick = {
                                    updateFlashMode(ImageCapture.FLASH_MODE_ON)
                                    flashlightMenuExpanded = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Sharp.FlashOn,
                                        tint = Color.White,
                                        contentDescription = "Localized description"
                                    )
                                }
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
                                    imageVector = Icons.Sharp.Cached,
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


                                onCapture()


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


