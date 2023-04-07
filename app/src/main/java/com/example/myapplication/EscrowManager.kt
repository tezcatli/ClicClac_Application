package com.example.myapplication

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
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

    suspend fun init() {
        cipherEscrow.init(appContext, URL("http://10.0.2.2:5000/certificate"))
    }

    fun add(dateTime : ZonedDateTime) : SecretKey {
        val uuid = UUID.randomUUID().toString()
        cipherEscrow.escrow(dateTime, uuid)

        val escrow = cipherEscrow.escrow(dateTime, uuid)

        databaseEscrow.escrowDbDao().insertAll(EscrowDbEntry(uuid, dateTime, escrow.token, escrow.wrappedKey))

        return cipherEscrow.getsKeyEnc(uuid)
    }

    fun listExpired(): List<EscrowDbEntry> {
        return databaseEscrow.escrowDbDao().findExpired(ZonedDateTime.now())
    }

    fun recover(uuid : String) : SecretKey {
        val escrow = databaseEscrow.escrowDbDao().findById(uuid)
        return cipherEscrow.getsKeyDec(escrow.UUID)
    }


}
