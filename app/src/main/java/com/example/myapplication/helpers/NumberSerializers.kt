package com.example.myapplication.helpers

class NumberSerializers {
    companion object {
        /*

         0x0123456789ABCDEF


         */

        fun numberToByteArray(number: Number): ByteArray {
            val size = when (number) {
                is Byte -> 1
                is Int -> 4
                is Long -> 8
                else -> 0
            }

            val buffer = ByteArray(size)
            var register: Long = number.toLong()

            (0 until size).forEach {
                buffer[size - 1 - it] = (register and 0xff).toByte()
                register = register shr 8
            }
            return buffer
        }



        fun byteArrayToNumber(buffer: ByteArray): Number {

            var register : Long = 0

            (buffer.indices).forEach {
                register = ((register shl 8) or (buffer[it].toLong() and 0xff))
            }

            val result : Number = when (buffer.size) {
                1 -> register.toByte()
                2 -> register.toShort()
                4 -> register.toInt()
                8 -> register.toLong()
                else -> throw (Exception("Wrong ByteArray size: " + buffer.size))
            }
            return result
        }
    }
}