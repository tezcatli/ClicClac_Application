package com.example.myapplication


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.URL
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

@RunWith(AndroidJUnit4::class)
class EscrowManagerInstrumentedTest {

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun escrowManagerTest() = runTest {

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val escrowManager = EscrowManager(appContext, this)

        escrowManager.init(URL("http://10.0.2.2:5000"))

        val uuid = escrowManager.add(ZonedDateTime.parse("2022-12-03T10:15:30+01:00[Europe/Paris]"))

        val testVector = "Salut Les Amis, comment allez vous ??? Moi Très bien".toByteArray()


        val ostream = escrowManager.setupOutputStream("testFile", uuid)
        ostream.write(testVector)
        ostream.close()

        val istream = escrowManager.setupInputStream("testFile", uuid)
        val compareVector = istream.readBytes()
        istream.close()

        Assert.assertArrayEquals(testVector, compareVector)

    }
}