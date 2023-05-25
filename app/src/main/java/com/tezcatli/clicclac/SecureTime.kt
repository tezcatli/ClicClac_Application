package com.tezcatli.clicclac

import android.os.SystemClock.elapsedRealtime
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration


@Singleton
class SecureTime @Inject constructor() {
    fun byteArrayToNumber(buffer: ByteArray): ULong {

        var register: ULong = 0UL

        (buffer.indices).forEach {
            register = ((register shl 8) or (buffer[it].toULong() and 0xffu))
        }

        return register
    }

    private val request = ubyteArrayOf(

        0xdbu, 0x00u, 0x11u, 0xe9u,
        0x00u, 0x00u, 0x00u, 0x00u,

        0x00u, 0x01u, 0x00u, 0x00u,
        0x00u, 0x00u, 0x00u, 0x00u,

        0x00u, 0x00u, 0x00u, 0x00u,
        0x00u, 0x00u, 0x00u, 0x00u,

        0x00u, 0x00u, 0x00u, 0x00u,
        0x00u, 0x00u, 0x00u, 0x00u,

        0x00u, 0x00u, 0x00u, 0x00u,
        0x00u, 0x00u, 0x00u, 0x00u,

        0x00u, 0x00u, 0x00u, 0x00u,
        0x00u, 0x00u, 0x00u, 0x00u,

        ).toByteArray()


    class SecureTimeException : RuntimeException()

    private fun getNtpZoneDateTime(): ZonedDateTime {
        var response = ByteArray(65536)

        DatagramSocket().use {
            it.soTimeout = 5000
            it.connect(InetAddress.getByName("time.nist.gov"), 123)

            it.send(DatagramPacket(request, request.size))
            it.receive(DatagramPacket(response, response.size))

        }

        return ZonedDateTime.parse("1900-01-01T00:00:00+00:00")
            .plusSeconds(byteArrayToNumber(response.copyOfRange(40, 44)).toLong())
    }


    var lastNtpTime: ZonedDateTime = ZonedDateTime.parse("1900-01-01T00:00:00+00:00")
    var lastElapsedRealTimeOffset: Long = 0

    var inSync = false

    fun getTime(): ZonedDateTime {
        try {
            var realTime = elapsedRealtime() + lastElapsedRealTimeOffset

            if (Duration.between(lastNtpTime.toInstant(), Instant.ofEpochMilli(realTime))
                    .abs() > 2.days.toJavaDuration()
            ) {
                lastNtpTime = getNtpZoneDateTime()
                lastElapsedRealTimeOffset =
                    lastNtpTime.toInstant().toEpochMilli() - elapsedRealtime()
                realTime = lastElapsedRealTimeOffset + elapsedRealtime()
            }
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(realTime), ZoneOffset.UTC)
        } catch (e: Exception) {
            throw SecureTimeException()
        }
    }

    fun checkSync(): Boolean {
        val unsecureTime = ZonedDateTime.now()

        return Duration.between(unsecureTime.toInstant(), getTime().toInstant())
            .abs() < 5.minutes.toJavaDuration()
    }



/*
    companion object {
        @Volatile
        private var Instance: SecureTime? = null
        fun getInstance(context: Context): SecureTime {
            return SecureTime.Instance ?: synchronized(this) {
                return SecureTime(context).also { SecureTime.Instance = it }
            }
        }
    }
    */
}

