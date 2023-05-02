package com.tezcatli.clicclac.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import java.time.Duration
import java.time.ZonedDateTime
import androidx.compose.ui.unit.sp


@Composable
fun EscrowedList(
    modifier: Modifier = Modifier,
    viewModel: EscrowedListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClickExpired: () -> Unit = {}
) {
    val listAllState by viewModel.listAllState.collectAsState()

    EscrowedList2(modifier, viewModel.expiredNumber, listAllState, onClickExpired)

}

@Composable
fun EscrowedList2(
    modifier: Modifier = Modifier,
    expiredNumber: Int = 0,
    escrowedListState: List<EscrowedState> = listOf(),
    onClickExpired: () -> Unit = {}
) {

    //val pendingListState by viewModel.pendingListState.collectAsState()
    //val expiredListState by viewModel.expiredListState.collectAsState()
    Column {
        OutlinedCard(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .clickable(enabled = true) { onClickExpired() }
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ready to be developed: $expiredNumber",
                    fontSize = 15.sp
                )
            }
        }
        LazyColumn(modifier = modifier) {
            // Add a single item

            val list = escrowedListState.map {
                Duration.between(
                    ZonedDateTime.now().toInstant(),
                    it.deadline.toInstant()
                ).run {
                    when {
                        this.isNegative -> null
                        this.toMinutes() == 0L -> (this.toNanos() / 1000000000L).toString() + " Seconds"
                        this.toHours() == 0L -> this.toMinutes().toString() + " Minutes"
                        this.toHours() in 1..48 -> this.toHours().toString() + " Hours"
                        else -> (this.toHours() / 24).toString() + " Days"
                    }
                }
            }.filterNotNull()


            items(list) { element ->
                OutlinedCard(
                    modifier = modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(all = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = element
                        )
                    }
                }
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
                deadline = ZonedDateTime.parse("2023-05-26T09:29:12.20481+02:00")
            ),
            EscrowedState(
                uuid = " 0262d08e-5321-4129-870b-83b9fa3dad80",
                deadline = ZonedDateTime.parse("2023-05-26T09:29:12.20481+02:00")
            )
        )
    )
}






