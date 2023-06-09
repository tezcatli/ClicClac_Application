package com.tezcatli.clicclac.crypto

import android.util.Log
import com.tezcatli.clicclac.CipherInputStreamProcessor
import com.tezcatli.clicclac.CipherOutputStreamProcessor
import com.tezcatli.clicclac.helpers.NumberSerializers
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/*

    CHUNK
      HEADER
        INT: IV SIZE
//        INT: TAG SIZE
        BYTE[]: IV
        CONTENT SIZE
      CONTENT
        BYTE[]
        TAG


 */

data class ChunkHeader(
    var contentSize: Int = 0,
    var ivSize: Int = 0,
//    var tagSize : Int = 0,
    var iv: ByteArray = ByteArray(0)
)


class CipherInputStream(
    private val inputStream: InputStream,
    val inputStreamProcessor: CipherInputStreamProcessor
) : InputStream() {

    var cipheredBuffer = ByteArray(0)
    var chunkBuffer = ByteArray(0)
    var chunkOffset: Int = 0
    var chunkHeader: ChunkHeader = ChunkHeader()

    override fun read(): Int {
        val tmp = ByteArray(1)
        val ret = read(tmp)
        return if (ret == -1)
            ret
        else
            tmp[0].toInt()
    }

    private fun readNBytes(buffer: ByteArray) {
        for (i in buffer.indices) {
            val byte = inputStream.read()
            if (byte == -1) {
                throw IOException("Unexpected end of stream")
            }
            buffer[i] = byte.toByte()
        }
    }

    private fun readInt(): Int {
        val buffer = ByteArray(4)
        readNBytes(buffer)
        return NumberSerializers.byteArrayToNumber(buffer).toInt()
    }

    fun readNewChunk(): Int {

     //   Log.e("CLICCLAC", "readNewChunk")
        chunkOffset = 0
        var chunkReadSize = 0


        try {

            try {
                chunkHeader.ivSize = readInt()
            } catch (e: IOException) {
                Log.e("CLICCLAC", "readNewChunk : EOF")
                return -1
            }
            //       chunkHeader.tagSize = readInt()
            chunkHeader.iv = ByteArray(chunkHeader.ivSize)
            readNBytes(chunkHeader.iv)
            chunkHeader.contentSize = readInt()

            if (chunkBuffer.size < chunkHeader.contentSize) {
                cipheredBuffer =
                    ByteArray(chunkHeader.contentSize + inputStreamProcessor.getByteOverhead())
                chunkBuffer = ByteArray(chunkHeader.contentSize)
                chunkBuffer.fill(0xbb.toByte())
            }


            while (chunkReadSize != chunkHeader.contentSize) {
//            val tmpChunkBuffer = ByteArray(chunkHeader.contentSize - chunkReadSize)
                val res = inputStream.read(
                    cipheredBuffer,
                    chunkReadSize,
                    chunkHeader.contentSize - chunkReadSize
                )
                if (res == -1) {
                    throw Exception(IOException("Unexpected end of stream"))
                }
                chunkReadSize += res
            }
     //       Log.e("CLICCLAC", "readNewChunk : read $chunkReadSize")

            inputStream.read(cipheredBuffer, chunkReadSize, inputStreamProcessor.getByteOverhead())

            inputStreamProcessor.setUpBlock(chunkHeader.iv)
            inputStreamProcessor.decrypt(
                cipheredBuffer,
                chunkReadSize + inputStreamProcessor.getByteOverhead(),
                chunkBuffer
            )

//            if (chunkBuffer[chunkReadSize-40] == 0xbb.toByte() &&
//                chunkBuffer[chunkReadSize-30] == 0xbb.toByte() &&
//                chunkBuffer[chunkReadSize-20] == 0xbb.toByte() &&
//                chunkBuffer[chunkReadSize-10] == 0xbb.toByte()) {
//                Log.e("CLICCLAC", "last 4 bytes null")
//            }

        } catch (e: Exception) {
            Log.e("CLICCLAC", "readChunk : $e")
            throw (e)
        }
        return chunkReadSize

    }

    override fun read(buffer: ByteArray?, off: Int, len: Int): Int {
        if (buffer == null) {
            throw Exception("Null buffer received")
        }

        var off2: Int = off
        var read = 0

    //    Log.e("CLICCLAC", "read off = $off, len = $len")


        while (read != len) {

            if (chunkOffset == chunkHeader.contentSize) {
                if (readNewChunk() == -1)
                    if (read != 0) {
                        chunkOffset = 0
                        chunkHeader.contentSize = 0
             //           Log.e("CLICCLAC", "read return = $read")

                        return read
                    } else {
               //         Log.e("CLICCLAC", "read return = -1")
                        return -1
                    }
            }

            if ((len - read) >= (chunkHeader.contentSize - chunkOffset)) {
                chunkBuffer.copyInto(buffer, off2, chunkOffset, chunkHeader.contentSize)
                off2 += chunkHeader.contentSize - chunkOffset
                read += chunkHeader.contentSize - chunkOffset
                chunkOffset += chunkHeader.contentSize - chunkOffset
            } else {
                try {
                    chunkBuffer.copyInto(buffer, off2, chunkOffset, chunkOffset + len - read)
                } catch (e: Exception) {
                    throw (e)
                }
                chunkOffset += len - read
                off2 += len - read
                read += len - read
            }
        }

  //      Log.e("CLICCLAC", "read return = $read")

        return read
    }

    override fun read(buffer: ByteArray?): Int {
        return read(buffer, 0, buffer!!.size)
    }

    override fun markSupported(): Boolean {
        return false
    }

    override fun close() {
        inputStream.close()
        super.close()
    }

    init {
        inputStreamProcessor.init()
    }
}


class CipherOutputStream(
    private val outputStream: OutputStream,
    private val outputStreamProcessor: CipherOutputStreamProcessor,
    var chunkSize: Int = 1024 * 256
) : OutputStream() {

    var plainBuffer = ByteArray(chunkSize)
    var cipherBuffer = ByteArray(chunkSize + outputStreamProcessor.getByteOverhead())

    // var chunkOffset : Int = 0
    var plainBufferOffset = 0
    var chunkHeader: ChunkHeader = ChunkHeader()


    private fun writeNBytes(buffer: ByteArray) {
        outputStream.write(buffer)
    }

    private fun writeNBytes(buffer: ByteArray, offset: Int, len: Int) {
        outputStream.write(buffer, offset, len)
    }

    private fun writeInt(value: Int) {
        writeNBytes(NumberSerializers.numberToByteArray(value))
    }

    fun writeChunk(size: Int, offset : Int = 0) {
        //    chunkOffset = 0


        chunkHeader.contentSize = size

        chunkHeader.iv = outputStreamProcessor.setUp()
        chunkHeader.ivSize = chunkHeader.iv.size



        val res = outputStreamProcessor.encrypt(plainBuffer, offset, size, cipherBuffer)

        if (res != size + outputStreamProcessor.getByteOverhead()) {
      //      throw Exception("Couille Dans le potage")
            val newSize = res - outputStreamProcessor.getByteOverhead()
            var writtenSize = 0
            //var offset2 = 0
            while (writtenSize != size) {
                val writeSize = if ((size - writtenSize) > newSize)
                    newSize
                else
                    size - writtenSize
                writeChunk(writeSize, writtenSize)
                writtenSize += writeSize
            }
            chunkSize = res
            return
        }

        writeInt(chunkHeader.iv.size)
        writeNBytes(chunkHeader.iv)
        writeInt(size)


        writeNBytes(cipherBuffer, 0, size + outputStreamProcessor.getByteOverhead())

        plainBufferOffset = 0

    }

    override fun write(buffer: ByteArray?, off: Int, len: Int) {
        var written = 0
        var off2 = off

        if (buffer == null) {
            throw Exception("Null buffer received")
        }

        while (written != len) {
            // if enough size available in plainBuffer
            if (len - written <= (chunkSize - plainBufferOffset)) {
                buffer.copyInto(plainBuffer, plainBufferOffset, off2, off2 + len - written)
                off2 += len - written
                plainBufferOffset += len - written
                written += len - written
            } else {
                // if not enough size available in plainBuffer
                // fill plainBuffer, write current chunk and emit a new one
                buffer.copyInto(
                    plainBuffer,
                    plainBufferOffset,
                    off2,
                    off2 + chunkSize - plainBufferOffset
                )
                off2 += chunkSize - plainBufferOffset
                written += chunkSize - plainBufferOffset
                writeChunk(chunkSize)
            }
        }
    }

    override fun flush() {
        writeChunk(plainBufferOffset)
        outputStream.flush()
        super.flush()
    }

    override fun close() {
        writeChunk(plainBufferOffset)
        outputStream.close()
        super.close()
    }


    override fun write(b: Int) {
        TODO("Not yet implemented")
    }

    init {
        cipherBuffer.fill(0xff.toByte())
        outputStreamProcessor.init()
    }

}




