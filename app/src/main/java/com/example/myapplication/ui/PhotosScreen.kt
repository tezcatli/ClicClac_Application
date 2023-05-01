package com.example.myapplication.ui

import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.myapplication.AppViewModelProvider
import com.example.myapplication.R


@RequiresApi(Build.VERSION_CODES.Q)
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
            Log.e("PHOTOSCREEN", "Adding bitmap : " + it.width.toString() +  " " + it.height.toString())
            Row(modifier = modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center) {
                ElevatedCard() {
                    Image(
                        modifier = modifier.height(300.dp),
                        bitmap = it, contentDescription = "Image Label"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun previewPhotoScreen() {
    val imageBitmap = ImageBitmap.imageResource(
        R.drawable.elerte_a_white_cat_dressed_as_seaman_playing_piano_266754e2_e92f_4f82_b2a6_87008386f126
    )
    val truc = (1..10).map { imageBitmap }
    val imageBitmapList : List<ImageBitmap> = truc
    PhotosScreen2(imageBitmapList = imageBitmapList)
}