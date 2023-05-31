package com.tezcatli.clicclac.ui

import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class CustomPainter constructor(
    private val image: ImageBitmap,

    ) : Painter() {
    override val intrinsicSize: Size
        get() = Size(image.width.toFloat(), image.height.toFloat())

    override fun DrawScope.onDraw() {
        drawImage(
            image,
            dstSize = IntSize(
                this@onDraw.size.width.roundToInt(),
                this@onDraw.size.height.roundToInt()
            )
        )
    }

}


@Composable
fun PhotosScreen(
    appViewModel: ClicClacAppViewModel,
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    PhotosScreen2(
        appViewModel,
        modifier = modifier,
        imageUrlList = viewModel.imageUriList
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen2(
    appViewModel: ClicClacAppViewModel,
    modifier: Modifier = Modifier,
    imageUrlList: List<Uri> = listOf(),
) {
    var imageUri by rememberSaveable { mutableStateOf<Uri>(Uri.EMPTY) }
    var zoom by rememberSaveable { mutableStateOf(false) }
    appViewModel.fullScreen = zoom

    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }

    val configuration = LocalConfiguration.current


    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    Column(modifier = modifier.fillMaxSize()) {

        val scope = rememberCoroutineScope()

        val scaffoldState =
            rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))


        if (zoom) {


            //val imgBitmap = BitmapFactory.decodeFile(imageUri.toString())

            val contentResolver = LocalContext.current.contentResolver

            val imgBitmap = remember(imageUri) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        contentResolver,
                        imageUri
                    )
                ).asImageBitmap()
            }


            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Blue)
            ) {

                val boxHeight = LocalDensity.current.run { maxHeight.toPx() }
                val boxWidth = LocalDensity.current.run { maxWidth.toPx() }


                val customPainter = remember { CustomPainter(imgBitmap) }


                //var imgWidth = imgBitmap.width
                //var imgHeight = imgBitmap.height
                var imgWidth = boxWidth
                var imgHeight = boxHeight
                var imgWidth0 by remember { mutableStateOf(imgWidth) }
                var imgHeight0 by remember { mutableStateOf(imgHeight) }

                var scale0 by remember {
                    mutableStateOf(
                        if ((boxWidth / boxHeight) < (imgWidth / imgHeight)) {
                            imgWidth0 =
                                imgBitmap.width.toFloat() * (boxHeight / imgBitmap.height)
                            imgHeight0 = boxHeight
                            boxWidth / imgWidth
                        } else {
                            imgWidth0 = boxWidth
                            imgHeight0 =
                                imgBitmap.height.toFloat() * (boxWidth / imgBitmap.width)
                            boxHeight / imgHeight
                        }
                        //1.0f

                    )
                }

                var scale by remember { mutableStateOf(scale0) }


                var offset by remember { mutableStateOf(Offset.Zero) }
                var transformOrigin by remember { mutableStateOf(TransformOrigin.Center) }





                Log.e(
                    "CLICCLAC",
                    "boxHeight = " + boxHeight + " , imgheight = " + imgHeight
                )
                Log.e(
                    "CLICCLAC",
                    "boxWidth = " + boxWidth + " , imgwidth = " + imgWidth
                )

                Image(
                    modifier = modifier
                        .fillMaxSize()
                        //                   .scale(scaleCorrection)
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { centroid, pan, gestureZoom, _ ->

                                    Log.e("CLICCLAC", "zoom = "+  gestureZoom +  " centroid = " + centroid.x  + " " + centroid.y
                                            + " pan = " + pan.x + " " + pan.y
                                        + " offset = " + offset.x + " " + offset.y)
                                    val oldScale = scale * scale0
                                    scale = (scale * gestureZoom).coerceIn(1.0f..100.0f)
                                    val scale1 = scale * scale0

                                    transformOrigin = TransformOrigin(
                                        centroid.x / boxWidth,
                                        centroid.y / boxHeight
                                    )

                                    val d = sqrt((-pan.x + centroid.x - boxWidth / 2).pow(2) + (-pan.y + centroid.y - boxHeight / 2).pow(2)) * (scale1 - oldScale) / scale0

                                    offset = Offset(
                                        when {
                                            (boxWidth - imgWidth0 * scale1) >= 0
                                            -> 0f

                                            (boxWidth - imgWidth0 * scale1) < 0 &&
                                                    offset.x + pan.x <= (boxWidth - imgWidth0 * scale1) / 2
                                            -> (boxWidth - imgWidth0 * scale1) / 2

                                            (boxWidth - imgWidth0 * scale1) < 0 &&
                                                    offset.x + pan.x > -(boxWidth - imgWidth0 * scale1) / 2
                                            -> -(boxWidth - imgWidth0 * scale1) / 2

                                            else -> offset.x + pan.x  - if (gestureZoom != 1.0f) ((offset.x  + pan.x +centroid.x - boxWidth / 2 ) * gestureZoom -
                                                                        (offset.x + centroid.x - boxWidth / 2 )) / gestureZoom else 0f
                                        },
                                        when {
                                            (boxHeight - imgHeight0 * scale1) >= 0
                                            -> 0f

                                            (boxHeight - imgHeight0 * scale1) < 0 &&
                                                    offset.y + pan.y <= (boxHeight - imgHeight0 * scale1) / 2
                                            -> (boxHeight - imgHeight0 * scale1) / 2

                                            (boxHeight - imgHeight0 * scale1) < 0 &&
                                                    offset.y + pan.y > -(boxHeight - imgHeight0 * scale1) / 2
                                            -> -(boxHeight - imgHeight0 * scale1) / 2

                                            else -> offset.y + pan.y - if (gestureZoom != 1.0f) ((offset.y  +  pan.y + centroid.y - boxHeight / 2 )  * gestureZoom -
                                                                        (offset.y + centroid.y - boxHeight / 2 )) / gestureZoom else 0f
                                        }
                                    )
                                }
                            )
                        }


                        .graphicsLayer {
                            clip = false
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                            //this.transformOrigin = transformOrigin

                        }

                        .background(Color.Black)
                        .clickable {
                            zoom = false
                        },
                    contentScale = ContentScale.Fit,
                    //   painter = customPainter,
                    bitmap = imgBitmap,
                    contentDescription = null
                )
            }
        }
        LazyVerticalGrid(
            modifier = modifier.run {

                if (zoom) {
                    this.requiredSize(0.dp)
                } else {
                    this
                }
            },
            columns = GridCells.Adaptive(minSize = 128.dp)

        ) {

            items(imageUrlList) {
                Row {
                    AsyncImage(
                        modifier = modifier
                            .aspectRatio(1.0f)
                            .padding(all = 2.dp)
                            .clickable {
                                Log.e(
                                    "CLICCLAC",
                                    "TOGGLING FULL SCREEN : " + appViewModel.fullScreen.toString()
                                )
                                imageUri = it
                                zoom = true
                            },
                        model = it,
                        contentScale = ContentScale.Crop,
                        contentDescription = null
                    )
                }
            }
        }
    }
}


