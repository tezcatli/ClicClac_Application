package com.example.myapplication

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyNotYetValidException
import android.security.keystore.KeyProperties
import android.util.Log
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


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Test the following sequence :  generate escrow key,  recover it from remote and local.
 */
@RunWith(AndroidJUnit4::class)
class EscrowCipherInstrumentedTest {
    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun escrowUnescrow() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val cipherEscrow = EscrowCipher(appContext)
        cipherEscrow.init(URL("http://10.0.2.2:5000"))

        val uuid = UUID.randomUUID().toString()

        val escrow = cipherEscrow.escrow(ZonedDateTime.parse("2022-12-03T10:15:30+01:00[Europe/Paris]"), uuid)

        val sKey = cipherEscrow.withdraw(listOf(escrow))

        val testVector = "Salut Les Amis".toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, cipherEscrow.getsKeyEnc(uuid))
        val iv = cipher.iv.copyOf()
        val cipheredText: ByteArray = cipher.doFinal(testVector)


        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, sKey[0], spec)
        assertArrayEquals(testVector, cipher.doFinal(cipheredText))

        cipher.init(Cipher.DECRYPT_MODE, cipherEscrow.getsKeyDec(uuid), spec)
        assertArrayEquals(testVector, cipher.doFinal(cipheredText))

    }

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun escrowUnescrowUnexpiredKeyRemote() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val cipherEscrow = EscrowCipher(appContext)
        cipherEscrow.init(URL("http://10.0.2.2:5000"))

        val uuid = UUID.randomUUID().toString()

        val escrow = cipherEscrow.escrow(ZonedDateTime.parse("2023-12-03T10:15:30+01:00[Europe/Paris]"), uuid)

        val sKey = cipherEscrow.withdraw(listOf(escrow))


        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, cipherEscrow.getsKeyEnc(uuid))

        assertEquals(sKey[0], null)
    }


    @Test(expected = KeyNotYetValidException::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun escrowUnescrowUnexpiredKeyLocal() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val cipherEscrow = EscrowCipher(appContext)
        cipherEscrow.init(URL("http://10.0.2.2:5000"))

        val uuid = UUID.randomUUID().toString()

        val testVector = "Salut Les Amis".toByteArray()

        val escrow = cipherEscrow.escrow(ZonedDateTime.parse("2023-12-03T10:15:30+01:00[Europe/Paris]"), uuid)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, cipherEscrow.getsKeyEnc(uuid))
        val iv = cipher.iv.copyOf()
        val cipheredText: ByteArray = cipher.doFinal(testVector)

        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, cipherEscrow.getsKeyDec(uuid), spec)
        assertArrayEquals(testVector, cipher.doFinal(cipheredText))

    }

    @Test
    fun test() = runTest {

        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null, null)

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            "wKey",
            KeyProperties.PURPOSE_WRAP_KEY or KeyProperties.PURPOSE_ENCRYPT

        ).run {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setRandomizedEncryptionRequired(true)
            setKeyValidityStart(
                Date.from(
                    ZonedDateTime.parse("2022-12-03T10:15:30+01:00").toInstant()
                )
            )
            build()
        }

        generator.init(parameterSpec)
        val wKey = generator.generateKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, wKey)

        for (alias in ks.aliases()) {
            Log.i("KEYSTORE", "Name: $alias")
            val entry = ks.getEntry(alias, null)
            for (attribute in entry.attributes) {
                Log.i("KEYSTORE ALIAS", "Attribute : " + attribute.name)
            }
        }

        assertEquals(true, true)
    }

    @Test
    fun test2() = runTest {
/*
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null, null)

        val key1 =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
                init(

                    KeyGenParameterSpec.Builder(
                        "testkey1",
                        KeyProperties.PURPOSE_WRAP_KEY or KeyProperties.PURPOSE_ENCRYPT

                    ).run {
                        setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        setRandomizedEncryptionRequired(false)
                        build()
                    })
                generateKey()
            }

        val key2 =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
                init(

                    KeyGenParameterSpec.Builder(
                        "testkey2",
                        KeyProperties.PURPOSE_WRAP_KEY or KeyProperties.PURPOSE_ENCRYPT

                    ).run {
                        setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        setRandomizedEncryptionRequired(false)
                        build()
                    })
                generateKey()
            }

        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.WRAP_MODE, key1)
        cipher.wrap(key2)


        //val ciphertext: ByteArray = cipher.doFinal("Salut Les Amis ! ".toByteArray())

        for (alias in ks.aliases()) {
            Log.i("KEYSTORE", "Name: $alias")
            val entry = ks.getEntry(alias, null)
            for (attribute in entry.attributes) {
                Log.i("KEYSTORE ALIAS", "Attribute : " + attribute.name)
            }
        }
*/
        assertEquals(true, true)
    }
}