package com.suihan74.hatena.account.serializer

import com.suihan74.hatena.serializer.BooleanAsBinarySerializer
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

internal class BooleanAsBinarySerializerTest {
    private val serializer = BooleanAsBinarySerializer()

    @Test
    fun encode() {
        Assert.assertEquals("1", Json.encodeToString(serializer, true))
        Assert.assertEquals("0", Json.encodeToString(serializer, false))
    }

    @Test
    fun decode() {
        Assert.assertEquals(true, Json.decodeFromString(serializer, "1"))
        Assert.assertEquals(false, Json.decodeFromString(serializer, "0"))
    }

    @Test
    fun decodeOtherIntegers() {
        Assert.assertEquals(true, Json.decodeFromString(serializer, "2"))
        Assert.assertEquals(true, Json.decodeFromString(serializer, "-1"))
    }
}