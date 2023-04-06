package com.example.myapplication

import androidx.room.*


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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
    fun dateToTimestamp(date: ZonedDateTime): Long? {
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
    fun getAll(): List<EscrowDbEntry>

    @Query("SELECT * FROM EscrowDbEntry WHERE uuid = (:uuid)")
    fun findById(uuid: String): EscrowDbEntry

    @Query("SELECT * FROM EscrowDbEntry WHERE deadline > (:deadline)")
    fun findExpired(deadline: ZonedDateTime): List<EscrowDbEntry>
    @Insert
    fun insertAll(vararg users: EscrowDbEntry)
}


@Database(entities = [EscrowDbEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class EscrowDb : RoomDatabase() {
    abstract fun escrowDbDao(): EscrowDbDao
}


