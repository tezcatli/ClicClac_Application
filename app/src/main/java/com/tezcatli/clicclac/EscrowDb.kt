package com.tezcatli.clicclac

import androidx.room.*


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): ZonedDateTime? {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneOffset.UTC)
    }

    @TypeConverter
    fun dateToTimestamp(date: ZonedDateTime): Long {
        return date.toEpochSecond()
    }
}


@Entity(indices = [Index(value = ["deadline"])])
data class EscrowDbEntry(
    @PrimaryKey @ColumnInfo(name = "uuid") val UUID: String,
    @ColumnInfo val deadline : ZonedDateTime,
    @ColumnInfo val token: String,
    //@ColumnInfo(name = "s_key_name") val sKeyName: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "wrapped_key") val wrappedKey: ByteArray,
//    @ColumnInfo(name = "file_name") val fileName: String
)


@Dao
interface EscrowDbDao {
    @Query("SELECT * FROM EscrowDbEntry")
    suspend fun getAll(): List<EscrowDbEntry>

    @Query("SELECT * FROM EscrowDbEntry WHERE uuid = (:uuid)")
    suspend fun findById(uuid: String): EscrowDbEntry

    @Query("DELETE FROM EscrowDbEntry WHERE uuid = (:uuid)")
    suspend fun deleteById(uuid: String)

    @Query("SELECT * FROM EscrowDbEntry WHERE deadline < (:deadline)")
    suspend fun findExpired(deadline: ZonedDateTime): List<EscrowDbEntry>

    @Query("SELECT * FROM EscrowDbEntry WHERE deadline < (:deadline)")
    fun findExpiredF(deadline: ZonedDateTime): Flow<List<EscrowDbEntry>>

    @Query("SELECT * FROM EscrowDbEntry WHERE deadline >= (:deadline) ORDER by deadline")
    suspend fun findPending(deadline: ZonedDateTime): List<EscrowDbEntry>

    @Query("SELECT * FROM EscrowDbEntry WHERE deadline >= (:deadline) ORDER by deadline")
    fun findPendingF(deadline: ZonedDateTime): Flow<List<EscrowDbEntry>>

    @Query("SELECT * FROM EscrowDbEntry ORDER by deadline")
    suspend fun findAll(): List<EscrowDbEntry>

    @Query("SELECT * FROM EscrowDbEntry ORDER by deadline")
    fun findAllF(): Flow<List<EscrowDbEntry>>

    @Insert
    suspend fun insertAll(vararg users: EscrowDbEntry)
}


@Database(entities = [EscrowDbEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class EscrowDb : RoomDatabase() {
    abstract fun escrowDbDao(): EscrowDbDao
}
