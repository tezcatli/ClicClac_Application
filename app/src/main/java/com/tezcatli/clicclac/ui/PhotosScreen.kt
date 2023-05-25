package com.tezcatli.clicclac.ui

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
    var imageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var zoom by remember { mutableStateOf(false) }
    appViewModel.fullScreen = zoom

    Log.e("CLICCLAC", "appViewModel.fullscreen = " + appViewModel.fullScreen)

    Column {


        if (zoom) {
            AsyncImage(
                modifier = modifier
                    .fillMaxSize()
                    .clickable {
                        zoom = false
                    },
                contentScale = ContentScale.FillHeight,
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
                        modifier = modifier
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
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/*
@Preview
@Composable
fun PreviewPhotoScreen() {
    val imageBitmap = ImageBitmap.imageResource(
        R.drawable.elerte_a_white_cat_dressed_as_seaman_playing_piano_266754e2_e92f_4f82_b2a6_87008386f126
    )
    val truc = (1..10).map { imageBitmap }
    val imageBitmapList : List<ImageBitmap> = truc
    PhotosScreen2(imageBitmapList = imageBitmapList)
}
*/
