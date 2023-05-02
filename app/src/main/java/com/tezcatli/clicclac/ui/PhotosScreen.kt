package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import com.tezcatli.clicclac.R


@Composable
fun PhotosScreen(
    modifier : Modifier = Modifier,
    viewModel: PhotosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
   PhotosScreen2(
       imageBitmapList = viewModel.imageBitmapList)
}

@Composable
fun PhotosScreen2(
    modifier : Modifier = Modifier,
    imageBitmapList : List<ImageBitmap> = listOf()
) {

    LazyColumn(modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        items(imageBitmapList) {
            Log.d("CLICCLAC", "Adding bitmap : " + it.width.toString() +  " " + it.height.toString())
            Row(modifier = modifier.padding(vertical = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center) {
                ElevatedCard() {
                    Image(
                        modifier = modifier.fillMaxWidth(),
                        bitmap = it, contentDescription = "Image Label"
                    )
                }
            }
        }
    }
}

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