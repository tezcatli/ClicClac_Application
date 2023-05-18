package com.tezcatli.clicclac.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import com.tezcatli.clicclac.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigCassetteScreen(
    modifier : Modifier = Modifier,
    viewModel: ConfigCassetteViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSubmit : ()->Unit = {}
) {
    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        ElevatedCard(modifier = Modifier.padding(all = 20.dp)) {
            Column(modifier = Modifier.padding(all = 20.dp)) {
                // Row(horizontalArrangement = Arrangement.Center) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Change deadline",
                        fontSize = 30.sp,
                    )
                    Text(
                        text = "Instructions",
                        fontSize = 20.sp,
                    )
                    Divider(modifier = Modifier.padding(vertical = 5.dp))
                }
                //  }
                Text(
                    text = stringResource(id = R.string.delay_change_instruction)
                )

//val truc = ConfigViewModel::setDeadline
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.cassetteDevelopmentDelayChange,
                    isError = !viewModel.cassetteDevelopmentDelayValid,
                    onValueChange = { change -> viewModel.validateDevelopmentDelay(change) },

                    label = { Text(stringResource(R.string.config_cassette_screen_change_development_delay)) })

                Text(
                    text = stringResource(id = R.string.number_of_shots_per_days_change_instruction)
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.shotsPerDaysChange,
                    isError = !viewModel.shotsPerDaysValid,
                    onValueChange = { change -> viewModel.validateShotsPerDays(change) },

                    label = { Text(stringResource(R.string.config_cassette_screen_change_number_of_shots_per_days)) })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        enabled = viewModel.formValid,
                        onClick = {
                            viewModel.submitForm()
                            onSubmit()
                        }) {
                        Text(text = stringResource(R.string.config_cassette_screen_submit))
                    }
                }
            }
        }
    }
}

