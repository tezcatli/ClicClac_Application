package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.AppViewModelProvider
import java.time.Duration
import java.time.ZonedDateTime


@Composable
fun EscrowedList(
    modifier: Modifier = Modifier,
    viewModel: EscrowedListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClickExpired: () -> Unit = {}
) {
    val listAllState by viewModel.listAllState.collectAsState()
    Column {
        Text(
            modifier = Modifier.clickable(enabled = true) {onClickExpired()},
            text = viewModel.expiredNumber.toString()
        )
        EscrowedList2(modifier, listAllState)
    }
}

@Composable
fun EscrowedList2(
    modifier: Modifier = Modifier,
    escrowedListState: List<EscrowedState> = listOf()
) {

    //val pendingListState by viewModel.pendingListState.collectAsState()
    //val expiredListState by viewModel.expiredListState.collectAsState()

    LazyColumn(modifier = modifier) {
        // Add a single item

        items(escrowedListState) { element ->
            Row {
                val text: String =
                    Duration.between(ZonedDateTime.now().toInstant(), element.deadline.toInstant())
                        .run {
                            when {
                                this.isNegative -> "Ready to be developped"
                                this.toMinutes() == 0L -> (this.toNanos() / 1000000000L).toString() + " Seconds"
                                this.toHours() == 0L -> this.toMinutes().toString() + " Minutes"
                                this.toHours() in 1..48 -> this.toHours().toString() + " Hours"
                                else -> "Long time to wait"
                            }
                        }
                Text(
                    text = text
                )
            }
        }
    }
}


@Preview
@Composable
fun DefaultPreview2() {
    EscrowedList2(
        escrowedListState =
        listOf(
            EscrowedState(
                uuid = " 0262d08e-5321-4129-870b-83b9fa3dad80",
                deadline = ZonedDateTime.parse("2023-04-26T09:29:12.20481+02:00")
            ),
            EscrowedState(
                uuid = " 0262d08e-5321-4129-870b-83b9fa3dad80",
                deadline = ZonedDateTime.parse("2023-04-26T09:29:12.20481+02:00")
            ),
            EscrowedState(
                uuid = " 0262d08e-5321-4129-870b-83b9fa3dad80",
                deadline = ZonedDateTime.parse("2023-04-26T09:29:12.20481+02:00")
            ),
            EscrowedState(
                uuid = " 0262d08e-5321-4129-870b-83b9fa3dad80",
                deadline = ZonedDateTime.parse("2023-05-26T09:29:12.20481+02:00")
            )
        )
    )
}






