@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class,
    ExperimentalStdlibApi::class
)

package com.tezcatli.clicclac.ui


import android.content.ContentResolver
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tezcatli.clicclac.EscrowDbEntry
import com.tezcatli.clicclac.EscrowManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
// import java.time.ZonedDateTime
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


fun EscrowedStateFromDb(db: EscrowDbEntry): EscrowedState {
    return EscrowedState(uuid = db.UUID, deadline = db.deadline)
}


data class BucketsDef(
    val range: OpenEndRange<Duration>,
    val slotName: String,
    val print: (Duration) -> List<String>
)


data class Bucket(
    var list: MutableList<Duration> = mutableListOf()
)


class EscrowedListViewModel(
    private val escrowManager: EscrowManager,
    private val contentResolver: ContentResolver
) : ViewModel() {

    public val bucketsDef = listOf(
        BucketsDef(
            (-Duration.INFINITE).rangeUntil(Duration.ZERO),
            "ready to develop",
            { _ -> listOf<String>("Ready", "") }),
        BucketsDef(
            0.seconds.rangeUntil(1.minutes),
            "in less than 1 minute",
            { t -> listOf<String>(t.inWholeSeconds.toString(), "Seconds") }),
        BucketsDef(
            1.minutes.rangeUntil(10.minutes),
            "in less than 10 minute",
            { t -> listOf<String>(t.inWholeSeconds.toString(), "Seconds") }),
        BucketsDef(
            10.minutes.rangeUntil(1.hours),
            "in less than 1 hour",
            { t -> listOf<String>(t.inWholeMinutes.toString(), "Minutes") }),
        BucketsDef(
            1.hours.rangeUntil(4.hours),
            "in less than 4 hours",
            { t -> listOf<String>(t.inWholeMinutes.toString(), "Minutes") }),
        BucketsDef(
            4.hours.rangeUntil(1.days),
            "in less than 1 day",
            { t -> listOf<String>(t.inWholeHours.toString(), "Hours") }),
        BucketsDef(
            1.days.rangeUntil(7.days),
            "in less than 1 week",
            { t -> listOf<String>(t.inWholeHours.toString(), "Hours") }),
        BucketsDef(
            7.days.rangeUntil(Duration.INFINITE),
            "in coming weeks",
            { t -> listOf<String>(t.inWholeDays.toString(), "Days") })
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
            listBucket[bucketId(timeDiff)]!!.add(timeDiff)
        }
    }

    fun bucketId(timeDiff: Duration): Int {
        bucketsDef.forEachIndexed() { index, bucketDef ->
            // if (bucketDef.slot(timeDiff)) {
            if (timeDiff in bucketDef.range) {
                return index
            }
        }
        return -1
    }

    init {
        bucketsDef.forEach() { _ ->
            listBucket.add(mutableStateListOf())
        }


        viewModelScope.launch {
            var job: Job = viewModelScope.launch {}
            job.cancelAndJoin()
            escrowManager.listAllF().filterNotNull()
                .collect() {
                    if (job.isActive) {
                        job.cancelAndJoin()
                    }
       //             val truc = coroutineContext.job.children.toList().size
                    job = viewModelScope.launch {
                        Log.e("CLICCLAC", "RECEIVING listAllF ")
                        //               tmpList.add(0)
                        var deadLineList = mutableListOf<Duration>()

                        while (true) {

                            deadLineList.clear()
                            if (it.isNotEmpty()) {
                                nextEscrow =  ChronoUnit.MILLIS.between(ZonedDateTime.now(),
                                    it[0].deadline).milliseconds
                            }

                            bucketize(it.map { it -> it.deadline })

                            listBucket.forEachIndexed() { index, it ->
                                if (index != 0 && it.isNotEmpty()) {
                                    (it.first() - bucketsDef[index].range.start).apply {
                                        if (! this.isNegative())
                                            deadLineList.add(this)
                                    }
                                }
                            }

                            if (deadLineList.size != 0) {
                                if (it.isEmpty()) {
                                    Log.d("CLICCLAC", "Next deadline " + deadLineList[0])
                                    delay(deadLineList[0].plus(500.milliseconds))
                                } else {
                                    delay(1.0.seconds)
                                }
                            }  else {
                                break
                            }
                        }

                    }
                }
        }
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

    }
}

