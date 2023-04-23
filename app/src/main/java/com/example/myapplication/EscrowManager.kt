package com.example.myapplication

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.io.*
import java.net.URL
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.SecretKey
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object EscrowManagerModule {

    @Singleton
    @Provides
    fun provideManager(@ApplicationContext context: Context): EscrowManager {
        return EscrowManager(context)
    }

}

class EscrowManager(private val appContext : Context) {
    var cipherEscrow: EscrowCipher = EscrowCipher(appContext)

    var databaseEscrow = Room.databaseBuilder(
        appContext,
        EscrowDb::class.java, "database-name"
    ).build()

    suspend fun init(url: URL) {
        cipherEscrow.init(url)
        cleanUp()
    }

    fun add(dateTime: ZonedDateTime): String {
        val uuid = UUID.randomUUID().toString()
        cipherEscrow.escrow(dateTime, uuid)

        val escrow = cipherEscrow.escrow(dateTime, uuid)

        databaseEscrow.escrowDbDao()
            .insertAll(EscrowDbEntry(uuid, dateTime, escrow.token, escrow.wrappedKey))

        return uuid
    }

    fun listExpired(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findExpired(ZonedDateTime.now())
    }

    fun listPending(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findPending(ZonedDateTime.now())
    }

    fun recover(uuid: String): SecretKey {
        val escrow = databaseEscrow.escrowDbDao().findById(uuid)
        return cipherEscrow.getsKeyDec(escrow.UUID)
    }


    fun cleanUp() {
        // delete all db entries without associated keys
        cipherEscrow.cleanUp(databaseEscrow.escrowDbDao().getAll().map { it.UUID })

        // delete all keys without db entries
        for (key in cipherEscrow.listKeys()) {
            try {
                databaseEscrow.escrowDbDao().findById(key)
            } catch (e : java.lang.Exception) {
                cipherEscrow.deleteKey(key)
            }
        }
    }

    //fun buildEInputStream(fileName: String, uuid: String) : EInputStream {
    //    return EInputStream(fileName, uuid)
   // }

    inner class EInputStream(private val fileName: String, private val uuid: String) {
        var streamName : String
        var token : String

        val inputStream : InputStream = File(appContext.filesDir, fileName).inputStream().let {
            DataInputStream(it).run {
                token = readUTF()
            }
            cipherEscrow.setupInputStream(it, uuid).apply {
                DataInputStream(this).run {
                    streamName = readUTF()
                }
            }
        }
    }

    //fun buildEOutputStream(fileName: String, uuid: String, streamName : String) : EOutputStream {
    //    return EOutputStream(fileName, uuid, streamName)
    //}

    inner class EOutputStream(filename: String, uuid: String, streamName: String) {
        val token = databaseEscrow.escrowDbDao().findById(uuid).token

        val outputStream =  File(appContext.filesDir, filename).outputStream().let {
            DataOutputStream(it).run {
                writeUTF(token)
                flush()
            }
            cipherEscrow.setupOutputStream(it, uuid).apply {
                DataOutputStream(this).run {
                    writeUTF(streamName)
                    flush()
                }
            }
        }
    }
}