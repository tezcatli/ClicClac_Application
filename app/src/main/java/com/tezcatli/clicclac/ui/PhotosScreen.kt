package com.tezcatli.clicclac.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider


@Composable
fun PhotosScreen(
    appViewModel: ClicClaAppViewModel,
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    PhotosScreen2(
        appViewModel,
        contentResolver = viewModel.contentResolver,
        modifier = modifier,
        imageUrlList = viewModel.imageUriList
    )
}


@Composable
fun PhotosScreen2(
    appViewModel: ClicClaAppViewModel,
    contentResolver: ContentResolver,
    modifier: Modifier = Modifier,
    imageUrlList: List<Uri> = listOf(),
) {
    var imageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }



    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    Column() {

        if (appViewModel.fullScreen) {
            val source = ImageDecoder.createSource(contentResolver, imageUri)

            val image = try {
                ImageDecoder.decodeBitmap(source).asImageBitmap()
            } catch (e: Throwable) {
                Log.e("CLICCLAC", "Exception caught on $e")
                null
            }
            if (image != null) {
                if (image != null) {
                    Image(
                        modifier = modifier
                            .fillMaxSize()
                            .clickable {
                                appViewModel.fullScreen = false
                            },
                        contentScale = ContentScale.Fit,
                        bitmap = image, contentDescription = "Image Label"
                    )
                }
            }
        }


        LazyVerticalGrid(
            modifier = modifier.run {

                if (appViewModel.fullScreen) {
                    this.requiredSize(0.dp)
                } else {
                    this
                }
            },
            columns = GridCells.Adaptive(minSize = 128.dp)

        ) {

            items(imageUrlList) { it ->
                Row(
                ) {

                    val source = ImageDecoder.createSource(contentResolver, it)

                    image = try {
                        ImageDecoder.decodeBitmap(source).asImageBitmap()
                    } catch (e: Throwable) {
                        Log.e("CLICCLAC", "Exception caught on $e")
                        null
                    }
                    if (image != null) {
                        Image(
                            modifier = modifier
                                .padding(all = 2.dp)
                                .clickable {
                                    appViewModel.fullScreen = !appViewModel.fullScreen
                                    Log.e(
                                        "CLICCLAC",
                                        "TOGGLING FULL SCREEN : " + appViewModel.fullScreen.toString()
                                    )
                                    imageUri = it
                                },
                            contentScale = ContentScale.Fit,
                            bitmap = image!!, contentDescription = "Image Label"
                        )
                    }
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
