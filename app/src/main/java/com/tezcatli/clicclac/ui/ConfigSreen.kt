package com.tezcatli.clicclac.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import com.tezcatli.clicclac.BuildConfig


@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier.fillMaxWidth(),
    viewModel: ConfigViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onCassetteClick: () -> Unit = {}
) {
    val cassetteDevelopmentDelayState by viewModel.cassetteDevelopmentDelayState.collectAsState()
    val shotsPerDays by viewModel.shotsPerDays.collectAsState()


    ConfigScreen2(
        modifier = modifier,
        onCassetteClick = onCassetteClick,
        cassetteDevelopmentDelay = cassetteDevelopmentDelayState,
        shotsPerDays = shotsPerDays
        //   versionName = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen2(
    modifier: Modifier = Modifier,
    onCassetteClick: () -> Unit = {},
    cassetteDevelopmentDelay: String = "" ,
    shotsPerDays: Int = 10
) {
    //var textValue by remember { mutableStateOf("") }

//    Log.i("toto", BuildConfig.BUILD_TIME)

    Column {
        OutlinedCard(
        ) {
            Row(modifier = modifier
                .fillMaxWidth()
                .padding(all = 5.dp),
                horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "Clic Clac " + BuildConfig.VERSION_NAME + " (build " + BuildConfig.VERSION_CODE + " )"
                )
            }
        }

        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .clickable(enabled = true) { onCassetteClick() }
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
                    text = "Development delay: $cassetteDevelopmentDelay"
                )
                Text(
                    text = "Shots per days: $shotsPerDays"
                )
            }
        }
    }
}


@Preview
@Composable
fun ConfigScreenPreview() {
    ConfigScreen2()
}
