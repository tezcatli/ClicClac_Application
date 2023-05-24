package com.tezcatli.clicclac

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress.getByName
import java.time.ZonedDateTime





@RunWith(AndroidJUnit4::class)
class NtpTimeTest {
    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun ntpTime() = runTest {


        val st = SecureTime()

        Log.e("TEST",   st.getZoneDateTime().toString())


    }
}