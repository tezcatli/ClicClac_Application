@file:OptIn(
    ExperimentalStdlibApi::class,
)

package com.tezcatli.clicclac.ui


// import java.time.ZonedDateTime
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.EscrowManager
import com.tezcatli.clicclac.R
import com.tezcatli.clicclac.SecureTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


data class EscrowedState(
    val uuid: String = "",
    val deadline: ZonedDateTime = ZonedDateTime.parse("1980-01-01T00:00:00+00:00")
)


data class BucketsDef(
    val range: OpenEndRange<Duration>,
    val slotName: Int,
    val print: (Duration) -> List<String>
)

class EscrowedListViewModel(
    private val escrowManager: EscrowManager,
    val secureTime : SecureTime,
) : ViewModel() {

    val bucketsDef = listOf(
        BucketsDef(
            (-Duration.INFINITE).rangeUntil(Duration.ZERO),
            R.string.pending_photos_ready_to_be_developed,
            { listOf("Ready", "") }),
        BucketsDef(
            0.seconds.rangeUntil(1.minutes),
            R.string.pending_photos_less_than_1_minute,
            { t -> listOf(t.inWholeSeconds.toString(), "Seconds") }),
        BucketsDef(
            1.minutes.rangeUntil(10.minutes),
            R.string.pending_photos_less_than_10_minute,
            { t -> listOf(t.inWholeSeconds.toString(), "Seconds") }),
        BucketsDef(
            10.minutes.rangeUntil(1.hours),
            R.string.pending_photos_less_than_1_hour,
            { t -> listOf(t.inWholeMinutes.toString(), "Minutes") }),
        BucketsDef(
            1.hours.rangeUntil(4.hours),
            R.string.pending_photos_less_than_4_hours,
            { t -> listOf(t.inWholeMinutes.toString(), "Minutes") }),
        BucketsDef(
            4.hours.rangeUntil(1.days),
            R.string.pending_photos_less_than_1_day,
            { t -> listOf(t.inWholeHours.toString(), "Hours") }),
        BucketsDef(
            1.days.rangeUntil(7.days),
            R.string.pending_photos_less_than_1_week,
            { t -> listOf(t.inWholeHours.toString(), "Hours") }),
        BucketsDef(
            7.days.rangeUntil(Duration.INFINITE),
            R.string.pending_photos_in_coming_weeks,
            { t -> listOf(t.inWholeDays.toString(), "Days") })
    )


    var listBucket = mutableStateListOf<SnapshotStateList<Duration>>()

    var nextEscrow by mutableStateOf(Duration.ZERO)


    fun printBucketizedElement(time: Duration): List<String> {
        return bucketsDef[bucketId(time)].print(time)
    }

    private fun bucketize(list: List<ZonedDateTime>) {
        listBucket.forEach {
            it.clear()
        }
        val currentTime = ZonedDateTime.now().toInstant()
        list.forEach {
            val timeDiff = ChronoUnit.MILLIS.between(currentTime, it.toInstant()).milliseconds

            //       Log.e("CLICCLAC", "Adding $timeDiff to bucket id " + bucketId(timeDiff))
            listBucket[bucketId(timeDiff)].add(timeDiff)
        }
    }

    private fun bucketId(timeDiff: Duration): Int {
        bucketsDef.forEachIndexed { index, bucketDef ->
            // if (bucketDef.slot(timeDiff)) {
            if (timeDiff in bucketDef.range) {
                return index
            }
        }
        return -1
    }

    init {
        bucketsDef.forEach { _ ->
            listBucket.add(mutableStateListOf())
        }


        viewModelScope.launch {
            var job: Job = viewModelScope.launch {}
            job.cancelAndJoin()
            escrowManager.listAllF().filterNotNull()
                .collect {
                    if (job.isActive) {
                        job.cancelAndJoin()
                    }
                    //             val truc = coroutineContext.job.children.toList().size
                    job = viewModelScope.launch {
                        Log.e("CLICCLAC", "RECEIVING listAllF ")
                        //               tmpList.add(0)
                        val deadLineList = mutableListOf<Duration>()

                        while (true) {

                            deadLineList.clear()
                            if (it.isNotEmpty()) {
                                nextEscrow = ChronoUnit.MILLIS.between(
                                    ZonedDateTime.now(),
                                    it[0].deadline
                                ).milliseconds
                            }

                            bucketize(it.map { it.deadline })

                            listBucket.forEachIndexed { index, it ->
                                if (index != 0 && it.isNotEmpty()) {
                                    (it.first() - bucketsDef[index].range.start).apply {
                                        if (!this.isNegative())
                                            deadLineList.add(this)
                                    }
                                }
                            }

                            if (deadLineList.size != 0) {
                                if (! listBucket[0].isEmpty()) {
                                    Log.d("CLICCLAC", "Next deadline " + deadLineList[0])
                                    delay(deadLineList[0].plus(500.milliseconds))
                                } else {
                                    delay(1.0.seconds)
                                }
                            } else {
                                break
                            }
                        }
                    }
                }
        }
    }
}


