package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigCassetteScreen(
    viewModel: ConfigCassetteViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSubmit : ()->Unit = {}
) {
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
                text = DELAY_CHANGE_INSTRUCTION

            )
//val truc = ConfigViewModel::setDeadline
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.cassetteDevelopmentDelayChange,
                isError = !viewModel.cassetteDevelopmentDelayValid,
                onValueChange = { change -> viewModel.validateDevelopmentDelay(change) },
                keyboardActions = KeyboardActions(onNext = {
                    Log.e("TEST", "onDone")
                }),
                label = { Text("Change developpment delay") })

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = viewModel.cassetteDevelopmentDelayValid,
                    onClick = {
                        viewModel.setDevelopmentDelay()
                        onSubmit()
                    } ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

val DELAY_CHANGE_INSTRUCTION: String = """
Type a number and a duration.
Duration can be: minute, hour, day, month, year

Ex: "5 months" for 5 months delay
  
""".trimIndent()