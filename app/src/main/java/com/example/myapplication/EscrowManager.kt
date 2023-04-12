package com.example.myapplication

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.SecretKey

class EscrowManager(private val appContext : Context, private val externalScope: CoroutineScope) {
    var cipherEscrow: EscrowCipher = EscrowCipher(externalScope)

    var databaseEscrow = Room.databaseBuilder(
        appContext,
        EscrowDb::class.java, "database-name"
    ).build()

    suspend fun init(url: URL) {
        cipherEscrow.init(appContext, url)
    }

    fun add(dateTime : ZonedDateTime) : String {
        val uuid = UUID.randomUUID().toString()
        cipherEscrow.escrow(dateTime, uuid)

        val escrow = cipherEscrow.escrow(dateTime, uuid)

        databaseEscrow.escrowDbDao().insertAll(EscrowDbEntry(uuid, dateTime, escrow.token, escrow.wrappedKey))

        return uuid
    }


    fun setupInputStream(filename : String, uuid: String) : InputStream {
        val file = File(appContext.filesDir, filename)
        return cipherEscrow.setupInputStream(file.inputStream(), uuid)
    }

    fun setupOutputStream(filename : String, uuid: String) : OutputStream {
        val file = File(appContext.filesDir, filename)
        return cipherEscrow.setupOutputStream(file.outputStream(), uuid)
    }

    fun listExpired(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findExpired(ZonedDateTime.now())
    }

    fun recover(uuid : String) : SecretKey {
        val escrow = databaseEscrow.escrowDbDao().findById(uuid)
        return cipherEscrow.getsKeyDec(escrow.UUID)
    }


}
