package com.tezcatli.clicclac

import com.tezcatli.clicclac.helpers.NumberSerializers
import org.junit.Assert
import org.junit.Test

class NumberSerializersTest {
    @Test
    fun check_serializing() {

        val valueByte : Byte = 100
        val bufferByte = NumberSerializers.numberToByteArray(valueByte)
        Assert.assertEquals(valueByte, NumberSerializers.byteArrayToNumber(bufferByte))

        var valueInt = 0x012345678
        var bufferInt = NumberSerializers.numberToByteArray(valueInt)
        Assert.assertEquals(valueInt, NumberSerializers.byteArrayToNumber(bufferInt))

        valueInt = 1000000000
        bufferInt = NumberSerializers.numberToByteArray(valueInt)
        Assert.assertEquals(valueInt, NumberSerializers.byteArrayToNumber(bufferInt))

        val valueLong : Long = 0x0123456789ABCDEF
        val bufferLong = NumberSerializers.numberToByteArray(valueLong)
        Assert.assertEquals(valueLong, NumberSerializers.byteArrayToNumber(bufferLong))
    }
}

