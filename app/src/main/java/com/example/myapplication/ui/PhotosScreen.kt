package com.example.myapplication.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.AppViewModelProvider


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PhotosScreen(
    viewModel: PhotosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    LazyColumn {
        items(viewModel.bitmapList) {
            Image(it.asImageBitmap(),"")
        }
    }
}