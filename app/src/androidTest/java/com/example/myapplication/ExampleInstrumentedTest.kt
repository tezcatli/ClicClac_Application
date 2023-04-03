package com.example.myapplication

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
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
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() = runTest {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        //val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        //ks.load(null, null)

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)

        generator.init(256)

        val sKey = generator.generateKey()


        val cipherEscrow = CipherEscrow(this)
        cipherEscrow.init(URL("http://10.0.2.2:5000/certificate"))

        val escrow = cipherEscrow.escrow(ZonedDateTime.parse("2022-12-03T10:15:30+01:00[Europe/Paris]"))

        val sKey2 = cipherEscrow.withdraw(URL("http://10.0.2.2:5000/escrow"), listOf(escrow))

        //Log.i("TEST", Base64.getEncoder().encodeToString(sKey2[0].encoded))

        //assertEquals(sKey, sKey2[0])

        val testVector = "Salut Les Amis".toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sKey2[0].encoded, "AES"))
        val iv = cipher.iv.copyOf()
        val cipheredText: ByteArray = cipher.doFinal(testVector)


        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, escrow.sKeyHandle, spec)

        val plainText: ByteArray = cipher.doFinal(cipheredText)

        Log.e("TEST", "Plain text: " + String(testVector))
        Log.e("TEST", "Decrypted text: " + String(plainText))


        assertArrayEquals(testVector, plainText)

    }

    @Test
    fun test() = runTest {

        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null, null)

        var generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        var parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            "wKey",
            KeyProperties.PURPOSE_WRAP_KEY or KeyProperties.PURPOSE_ENCRYPT

        ).run {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setRandomizedEncryptionRequired(true)
            setKeyValidityStart(Date.from(ZonedDateTime.parse("2022-12-03T10:15:30+01:00").toInstant()))
            build()
        }

        generator.init(parameterSpec)
        val wKey = generator.generateKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, wKey)

        val ciphertext: ByteArray = cipher.doFinal("Salut Les Amis ! ".toByteArray())

        for (alias in ks.aliases()) {
            Log.i("KEYSTORE", "Name: $alias")
            val entry = ks.getEntry(alias, null)
            for (attribute in entry.attributes) {
                Log.i("KEYSTORE ALIAS",  "Attribute : " + attribute.name)
            }
        }

        assertEquals(true, true)
    }

}