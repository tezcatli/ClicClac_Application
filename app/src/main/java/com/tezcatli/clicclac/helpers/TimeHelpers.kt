package com.tezcatli.clicclac.helpers

import android.util.Log
import androidx.core.text.isDigitsOnly
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeHelpers {

    companion object {
        fun stringToDuration(deadline: String): Duration {
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
                    "day" -> value.days
                    "days" -> value.days
                    "hour" -> value.hours
                    "hours" -> value.hours
                    "minute" -> value.minutes
                    "minutes" -> value.minutes
                    "second" -> value.seconds
                    "seconds" -> value.seconds
                    else -> {
                        return 0.hours
                    }
                }
            }
            return duration
        }
    }
}