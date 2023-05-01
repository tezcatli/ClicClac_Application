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


@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier.fillMaxWidth(),
    viewModel: ConfigViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onCassetteClick: () -> Unit = {}
) {
    val cassetteDevelopmentDelayState by viewModel.cassetteDevelopmentDelayState.collectAsState()

    ConfigScreen2(
        modifier,
        onCassetteClick = onCassetteClick,
        cassetteDevelopmentDelayState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen2(
    modifier: Modifier = Modifier.fillMaxWidth(),
    onCassetteClick: () -> Unit = {},
    cassetteDevelopmentDelay: String = ""
) {
    //var textValue by remember { mutableStateOf("") }

    OutlinedCard(
        modifier = modifier.padding(all = 5.dp)
    ) {
        Column(
            modifier = modifier.padding(all = 10.dp)
        ) {

            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cassette configuration",
                    fontSize = 30.sp,
                )
                Divider(modifier = modifier.padding(vertical = 5.dp))
            }

            Text(
                modifier = Modifier.clickable(enabled = true) { onCassetteClick() },
                text = "Development delay: $cassetteDevelopmentDelay"
            )
        }
    }
}


@Preview
@Composable
fun ConfigScreenPreview() {
    ConfigScreen2()
}
