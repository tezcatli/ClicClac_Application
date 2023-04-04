package com.example.myapplication

import androidx.room.*


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}


@Entity(indices = [Index(value = ["deadline", "file_name"])])
data class EscrowedPicture(
    @PrimaryKey @ColumnInfo(name = "s_key_name") val sKeyName: String,
    @ColumnInfo val deadline : Date,
    @ColumnInfo val token: String,
    //@ColumnInfo(name = "s_key_name") val sKeyName: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "wrapped_key") val wrappedKey: ByteArray,
    @ColumnInfo(name = "file_name") val fileName: String
)


@Dao
interface EscrowedPicturesDao {
    @Query("SELECT * FROM EscrowedPicture")
    fun getAll(): List<EscrowedPicture>

    @Query("SELECT * FROM EscrowedPicture WHERE s_key_name = (:sKeyName)")
    fun findById(sKeyName: String): EscrowedPicture

    @Insert
    fun insertAll(vararg users: EscrowedPicture)
}


@Database(entities = [EscrowedPicture::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun escrowedPicturesDao(): EscrowedPicturesDao
}


