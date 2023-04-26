package com.example.myapplication

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ActivityScenario.launch
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var cipherEscrow: EscrowCipher = EscrowCipher(appContext)

    private var databaseEscrow = Room.databaseBuilder(
        appContext,
        EscrowDb::class.java, "database-name"
    ).build()


    suspend fun init(url: URL, onReady : () -> Unit) {
        withContext(Dispatchers.IO) {
            cipherEscrow.init(url)
            cleanUp()
        }
        onReady()

    }


    suspend fun init(url: URL) {
        cipherEscrow.init(url)
        cleanUp()
    }

    companion object {
        @Volatile
        private var Instance: EscrowManager? = null

        fun getInstance(context : Context) : EscrowManager {
            return Instance ?: synchronized(this) {
                return getInstance(context).also { Instance = it }
            }
        }



    }

    suspend fun add(dateTime: ZonedDateTime): String {
        val uuid = UUID.randomUUID().toString()
        cipherEscrow.escrow(dateTime, uuid)

        val escrow = cipherEscrow.escrow(dateTime, uuid)

        databaseEscrow.escrowDbDao()
            .insertAll(EscrowDbEntry(uuid, dateTime, escrow.token, escrow.wrappedKey))

        return uuid
    }

    suspend fun delete(uuid: String) {

        databaseEscrow.escrowDbDao().deleteById(uuid)
        cipherEscrow.deleteKey(uuid)

    }

   suspend fun listExpired(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findExpired(ZonedDateTime.now())
    }

    fun listExpiredF(): Flow<List<EscrowDbEntry>> {
        return databaseEscrow.escrowDbDao().findExpiredF(ZonedDateTime.now())
    }

    suspend fun listPending(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findPending(ZonedDateTime.now())
    }

    fun listPendingF(): Flow<List<EscrowDbEntry>> {
        return databaseEscrow.escrowDbDao().findPendingF(ZonedDateTime.now())
    }

    suspend fun recover(uuid: String): SecretKey {
        val escrow = databaseEscrow.escrowDbDao().findById(uuid)
        return cipherEscrow.getsKeyDec(escrow.UUID)
    }


    suspend fun cleanUp() {
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
        lateinit var streamName : String
        lateinit var token : String
        lateinit var inputStream : InputStream

        suspend fun build ()  : EInputStream {
            inputStream = File(appContext.filesDir, fileName).inputStream().let {
                DataInputStream(it).run {
                    token = readUTF()
                }
                cipherEscrow.setupInputStream(it, uuid).apply {
                    DataInputStream(this).run {
                        streamName = readUTF()
                    }
                }
            }
            return this
        }
    }

    //fun buildEOutputStream(fileName: String, uuid: String, streamName : String) : EOutputStream {
    //    return EOutputStream(fileName, uuid, streamName)
    //}

    inner class EOutputStream(val filename: String, val uuid: String,
                              val streamName: String) {

        lateinit var outputStream : OutputStream

        suspend fun build () : EOutputStream {
            val token = databaseEscrow.escrowDbDao().findById(uuid).token


            outputStream = File(appContext.filesDir, filename).outputStream().let {

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
            return this
        }
    }


}