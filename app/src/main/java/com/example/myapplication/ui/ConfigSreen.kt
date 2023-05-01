package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier.fillMaxWidth(),
    viewModel: ConfigViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onCassetteClick : ()->Unit = {}
) {
    //var textValue by remember { mutableStateOf("") }

    val cassetteDevelopmentDelayState by viewModel.cassetteDevelopmentDelayState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(

        ) {
            Text(
                modifier = Modifier.clickable(enabled = true) {onCassetteClick()},
                text = "Deadline: $cassetteDevelopmentDelayState"
            )
        }
    }
}

/*
@Composable
fun ConfigScreenContent(modifier : Modifier = Modifier) {

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(

        ) {
            Text(
                modifier = Modifier.clickable(enabled = true) {},
                text = "Deadline: $textValue"
            )
        }
    }
}



@Preview
@Composable
fun ConfigScreenPreview() {
    ConfigScreen()
}
*/