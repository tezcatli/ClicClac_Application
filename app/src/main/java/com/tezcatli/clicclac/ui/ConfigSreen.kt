package com.tezcatli.clicclac.ui

import android.icu.text.MessageFormat
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tezcatli.clicclac.BuildConfig
import com.tezcatli.clicclac.R
import com.tezcatli.clicclac.helpers.TimeHelpers.Companion.durationToString
import kotlin.time.Duration.Companion.seconds


@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier.fillMaxWidth(),
    viewModel: ConfigViewModel = hiltViewModel(),
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
    cassetteDevelopmentDelay: Long = 0,
    shotsPerDays: Int = 10
) {

    Column {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
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
                modifier = modifier
                    .padding(all = 10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.config_screen_cassette_configuration),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )

                Divider(modifier = modifier.padding(vertical = 5.dp))

                Text(
                    text = MessageFormat.format(
                        stringResource(R.string.config_screen_development_delay), durationToString(
                            LocalContext.current, cassetteDevelopmentDelay.seconds
                        )
                    )
                )

                Text(
                    text = MessageFormat.format(
                        stringResource(R.string.config_screen_shots_per_days),
                        shotsPerDays
                    )
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
