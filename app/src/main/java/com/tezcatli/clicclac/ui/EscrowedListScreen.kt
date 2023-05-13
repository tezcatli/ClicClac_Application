package com.tezcatli.clicclac.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tezcatli.clicclac.AppViewModelProvider
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds

/*
data class Bucket(
    var list: MutableList<Duration> = mutableListOf()
)
*/





@Composable
fun EscrowedList(
    modifier: Modifier = Modifier,
    viewModel: EscrowedListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClickExpired: () -> Unit = {}
) {
    Log.e("CLICCLAC", "RECOMPOSE ESCROWED LIST ")



    Column {
        val expired = viewModel.listBucket[0].size != 0

        OutlinedCard(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .clickable() {
                    if (expired) {
                        onClickExpired()
                    }
                },
            colors = CardDefaults.run {
                if (expired) {
                    cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                } else cardColors()
            }
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = if (expired) {
                        String.format("%s photo %s\nClick to develop", viewModel.listBucket[0].size.toString(), viewModel.bucketsDef[0].slotName)
                    } else {
                        if (viewModel.listBucket.map {
                            it -> it.size
                            }.sum() != 0) {

                            viewModel.nextEscrow.toComponents() { days, hours, minutes, seconds, _ ->
                                String.format(
                                    "Next photo in\n%02dd %02dh %02dm %02ds",
                                    days,
                                    hours,
                                    minutes,
                                    seconds
                                )
                            }
                        } else {
                            String.format("No photo submitted for development\n Press the camera icon on the bottom right of the screen to take pictures")
                        }
                    }
                )
            }
        }


        LazyColumn(modifier = modifier) {
            itemsIndexed(viewModel.listBucket) { index, el ->
                if (index != 0 && viewModel.listBucket[index].size != 0) {
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
                                text = viewModel.listBucket[index].size.toString() + " photos in " + viewModel.bucketsDef[index].slotName
                            )
                        }
                    }
                }
            }
        }

    }
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
        var ticks by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1.seconds)
                ticks++
            }
        }
        Text(
            text = "Countdown: $ticks",
            fontSize = 15.sp
        )
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






