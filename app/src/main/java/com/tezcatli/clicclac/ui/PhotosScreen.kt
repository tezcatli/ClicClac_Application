package com.tezcatli.clicclac.ui

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage


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

    Column {

        if (zoom) {

            var scale by remember { mutableStateOf(1f) }

            val state = rememberTransformableState { zoomChange, _, _ ->
                scale = (scale * zoomChange).coerceIn(1.0f..10.0f)
            }


            AsyncImage(
                modifier = modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,

                    )
                    // add transformable to listen to multitouch transformation events
                    // after offset
                    .transformable(state = state)

                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable {
                        zoom = false
                    },
                contentScale = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ContentScale.Fit
                } else {
                    ContentScale.Fit
                },
                model = imageUri,
                contentDescription = null
            )
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
                        modifier = modifier.aspectRatio(1.0f)
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

