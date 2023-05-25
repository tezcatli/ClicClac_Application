package com.tezcatli.clicclac

import android.content.Context
import androidx.room.Room
import com.tezcatli.clicclac.crypto.CipherInputStream
import com.tezcatli.clicclac.crypto.CipherOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.time.ZonedDateTime
import java.util.UUID
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton


/*
@Module
@InstallIn(SingletonComponent::class)
object EscrowManagerModule {

    @Singleton
    @Provides
    fun provideManager(@ApplicationContext context: Context): EscrowManager {
        return EscrowManager(context)
    }

}*/

@Singleton
class EscrowManager @Inject constructor(@ApplicationContext private val appContext: Context) {
    private var cipherEscrow: EscrowCipher = EscrowCipher(appContext)

    private var databaseEscrow = Room.databaseBuilder(
        appContext,
        EscrowDb::class.java, "database-name"
    ).build()


    suspend fun init(url: URL, onReady: () -> Unit) {
        if (CliClacApplication.DO_CRYPTO) {
            withContext(Dispatchers.IO) {
                cipherEscrow.init(url)
                cleanUp()
            }
        }
        onReady()

    }


    suspend fun init(url: URL) {
        if (CliClacApplication.DO_CRYPTO) {
            cipherEscrow.init(url)
            cleanUp()
        }
    }

    suspend fun add(dateTime: ZonedDateTime): String {
        val uuid = UUID.randomUUID().toString()

        if (CliClacApplication.DO_CRYPTO) {

            val escrow = cipherEscrow.escrow(dateTime, uuid)

            databaseEscrow.escrowDbDao()
                .insertAll(EscrowDbEntry(uuid, dateTime, escrow.token, escrow.wrappedKey))

        } else {
            databaseEscrow.escrowDbDao()
                .insertAll(EscrowDbEntry(uuid, dateTime, "", ByteArray(0)))
        }
        return uuid
    }

    suspend fun delete(uuid: String) {

        databaseEscrow.escrowDbDao().deleteById(uuid)
        if (CliClacApplication.DO_CRYPTO) {
            cipherEscrow.deleteKey(uuid)
        }
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


    suspend fun listAll(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findAll()
    }

    fun listAllF(): Flow<List<EscrowDbEntry>> {
        return databaseEscrow.escrowDbDao().findAllF()
    }

    suspend fun recover(uuid: String): SecretKey? {
        val escrow = databaseEscrow.escrowDbDao().findById(uuid)
        return cipherEscrow.getsKeyDec(escrow.UUID)
    }


    suspend fun cleanUp() {
        // delete all db entries without associated keys
       // cipherEscrow.cleanUp(databaseEscrow.escrowDbDao().getAll().map { it.UUID })

        // delete all keys without db entries
        for (key in cipherEscrow.listKeys()) {
            try {
                databaseEscrow.escrowDbDao().findById(key)
            } catch (e: java.lang.Exception) {
                cipherEscrow.deleteKey(key)
            }
        }
    }

    //fun buildEInputStream(fileName: String, uuid: String) : EInputStream {
    //    return EInputStream(fileName, uuid)
    // }

    inner class EInputStream(private val fileName: String, private val uuid: String) {
        lateinit var streamName: String
        lateinit var token: String
        lateinit var inputStream: InputStream

        fun build(): EInputStream {
            if (CliClacApplication.DO_CRYPTO) {
                inputStream = File(appContext.filesDir, fileName).inputStream().let {
                    DataInputStream(it).run {
                        token = readUTF()
                    }

                    val key = cipherEscrow.getsKeyDec(uuid)
                    CipherInputStream(it, CipherInputStreamProcessor(key!!)).run {
                        DataInputStream(this).run {
                            streamName = readUTF()
                        }
                        this
                    }
                }
                return this
            } else {
                inputStream = File(appContext.filesDir, fileName).inputStream().apply {
                    DataInputStream(this).run {
                        streamName = readUTF()
                    }
                }
                return this
            }
        }
    }

    fun deleteFile(fileName: String) {
        File(appContext.filesDir, fileName).delete()
    }


    //fun buildEOutputStream(fileName: String, uuid: String, streamName : String) : EOutputStream {
    //    return EOutputStream(fileName, uuid, streamName)
    //}

    inner class EOutputStream(
        val filename: String, val uuid: String,
        val streamName: String
    ) {

        lateinit var outputStream: OutputStream
       // lateinit var uuid : String

        suspend fun build(): EOutputStream {
            if (CliClacApplication.DO_CRYPTO) {
                val token = databaseEscrow.escrowDbDao().findById(uuid).token


                outputStream = File(appContext.filesDir, filename).outputStream().let {

                    DataOutputStream(it).run {
                        writeUTF(token)
                        flush()
                    }

                    val key = cipherEscrow.getsKeyEnc(uuid)

                    CipherOutputStream(
                        it,
                        CipherOutputStreamProcessor(key!!)
                    ).apply {
                        DataOutputStream(this).run {
                            writeUTF(streamName)
                            flush()
                        }
                    }
                }
                return this
            } else {
                outputStream = File(appContext.filesDir, filename).outputStream().apply {
                    DataOutputStream(this).run {
                        writeUTF(streamName)
                        flush()
                    }
                }
                return this
            }
        }
    }
}