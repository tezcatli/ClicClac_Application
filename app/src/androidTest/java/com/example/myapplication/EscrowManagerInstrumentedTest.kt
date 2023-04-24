package com.example.myapplication


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.time.ZonedDateTime
import java.util.*

@RunWith(AndroidJUnit4::class)
class EscrowManagerInstrumentedTest {

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun escrowManagerTest() = runTest {

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val escrowManager = EscrowManager(appContext)

        escrowManager.init(URL("http://10.0.2.2:5000"))

        val uuid = escrowManager.add(ZonedDateTime.parse("2022-12-03T10:15:30+01:00[Europe/Paris]"))

        val testVector = "Salut Les Amis, comment allez vous ??? Moi Très bien".toByteArray()
        val encryptedFileName = "Encrypted File Name"

        //val ostream = escrowManager.setupOutputStream("testFile", uuid)


        val ostream = escrowManager.EOutputStream("testFile", uuid,encryptedFileName).build()
        ostream.outputStream.write(testVector)
        ostream.outputStream.close()

        val istream = escrowManager.EInputStream("testFile", uuid).build()

        val compareVector = istream.inputStream.readBytes()
        istream.inputStream.close()

        Assert.assertArrayEquals(testVector, compareVector)
        Assert.assertEquals(encryptedFileName, istream.streamName)
    }
}