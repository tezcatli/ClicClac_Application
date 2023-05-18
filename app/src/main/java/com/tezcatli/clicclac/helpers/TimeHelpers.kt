package com.tezcatli.clicclac.helpers

import android.content.Context
import android.icu.text.MessageFormat
import android.util.Log
import androidx.core.text.isDigitsOnly
import com.tezcatli.clicclac.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeHelpers {

    companion object {
        fun stringToDuration(context : Context, deadline: String): Duration {
            //val splitExpression="\\d+\\w+(minutes?|hours?|months?years?)\\w+"
            var duration = 0.hours


            val split = deadline.trim().split("\\s+".toRegex())

            if (split[0] == "" || (split.size % 2) != 0) {
                return 0.hours
            }


            for (i in split.indices step 2) {
                Log.e("SPLIT", split[i])
                if (!split[i].isDigitsOnly()) {
                    return 0.hours
                }
                val value = split[i].toInt()

                duration += when (split[i + 1]) {
                    MessageFormat.format(context.getString(R.string.time_helpers_duration_day), value) -> value.days
                    MessageFormat.format(context.getString(R.string.time_helpers_duration_hour), value) -> value.hours
                    MessageFormat.format(context.getString(R.string.time_helpers_duration_minute), value) -> value.minutes
                    MessageFormat.format(context.getString(R.string.time_helpers_duration_second), value)-> value.seconds
                    else -> {
                        return 0.hours
                    }
                }
            }
            return duration
        }

        fun durationToString(context: Context, duration : Duration) : String {
            var duration2 = duration
            var deadline : String = ""
            while (duration2.inWholeSeconds != 0L) {
                when {
                    duration2.inWholeDays != 0L -> {
                        val quantity = duration2.inWholeDays
                        deadline += MessageFormat.format(" {0} " + context.getString(R.string.time_helpers_duration_day), quantity)
                        duration2 -= quantity.days
                    }
                    duration2.inWholeHours != 0L -> {
                        val quantity = duration2.inWholeHours
                        deadline += MessageFormat.format(" {0} " + context.getString(R.string.time_helpers_duration_hour), quantity)
                        duration2 -= quantity.hours
                    }
                    duration2.inWholeMinutes != 0L -> {
                        val quantity = duration2.inWholeMinutes
                        deadline += MessageFormat.format(" {0} " + context.getString(R.string.time_helpers_duration_minute), quantity)
                        duration2 -= quantity.minutes
                    }
                    else -> {
                        val quantity = duration2.inWholeSeconds
                        deadline +=  MessageFormat.format(" {0} " + context.getString(R.string.time_helpers_duration_second), quantity)
                        duration2 -= quantity.seconds
                    }
                }
            }
            return deadline.trim()
        }
    }
}