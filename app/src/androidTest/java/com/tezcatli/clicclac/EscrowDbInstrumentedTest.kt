package com.tezcatli.clicclac

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime
import java.util.*


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class EscrowDbInstrumentedTest {
    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun roomTest() = runTest {

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        appContext.deleteDatabase("database-name")

        val db = Room.databaseBuilder(
            appContext,
            EscrowDb::class.java, "database-name"
        ).build()

        val uuid = UUID.randomUUID()

        db.escrowDbDao().insertAll(EscrowDbEntry(
            UUID = uuid.toString(),
            deadline = ZonedDateTime.now(),
            token = "token",
            wrappedKey = byteArrayOf(),
    //       fileName = "fileName"

        ))

        for (escrowedPicture in db.escrowDbDao().getAll()) {
            Log.i("ROOM",   escrowedPicture.UUID + " " + escrowedPicture.deadline.toString())
        }

        val escrowedPicture = db.escrowDbDao().findById(uuid.toString())

        Log.i("ROOM",   escrowedPicture.UUID + " " + escrowedPicture.deadline.toString())

        assertEquals(true, true)

    }

}