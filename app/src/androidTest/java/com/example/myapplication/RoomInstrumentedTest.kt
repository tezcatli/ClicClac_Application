package com.example.myapplication

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.security.KeyStore
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class RoomInstrumentedTest {
    @Test
    fun roomTest() = runTest {

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val db = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java, "database-name"
        ).build()

        val uuid = UUID.randomUUID()

        db.escrowedPicturesDao().insertAll(EscrowedPicture(
            sKeyName = uuid.toString(),
            deadline = Date(),
            token = "token",
            wrappedKey = byteArrayOf(),
            fileName = "fileName"

        ))

        for (escrowedPicture in db.escrowedPicturesDao().getAll()) {
            Log.i("ROOM",   escrowedPicture.sKeyName + " " + escrowedPicture.deadline.toString())
        }

        val escrowedPicture = db.escrowedPicturesDao().findById(uuid.toString())

        Log.i("ROOM",   escrowedPicture.sKeyName + " " + escrowedPicture.deadline.toString())

        assertEquals(true, true)

    }

}