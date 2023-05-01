package com.example.myapplication.helpers

import android.util.Log
import androidx.core.text.isDigitsOnly
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TimeHelpers {

    companion object {
        fun stringToDuration(deadline: String): Duration {
            //val splitExpression="\\d+\\w+(minutes?|hours?|months?years?)\\w+"
            var duration = 0.hours


            var split = deadline.split(" ")

            if ((split.size % 2) != 0) {
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
                    else -> {
                        return 0.hours
                    }
                }
            }
            return duration
        }
    }
}