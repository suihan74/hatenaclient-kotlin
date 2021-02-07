package com.suihan74.hatena.serializer

import com.suihan74.hatena.bookmark.serializer.EpochTimeSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class EpochTimeSerializerTest {
    @Test
    fun encode_decode() {
        val serializer = EpochTimeSerializer()
        val src = OffsetDateTime.of(2021, 12, 6, 13, 30, 45, 0, ZoneOffset.ofHours(9)).toInstant()
        val encoded = Json.encodeToString(serializer, src)
        val decoded = Json.decodeFromString(serializer, encoded)
        assertEquals(src, decoded)
    }

    @Test
    fun boundary() {
        val serializer = EpochTimeSerializer()
        val decoded = Json.decodeFromString(serializer, "0")
        val expected = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
        assertEquals(expected, decoded)
    }
}