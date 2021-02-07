package com.suihan74.hatena.bookmark.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantISO8601SerializerTest {
    @Test
    fun encode() {
        val serializer = InstantISO8601Serializer()
        val src = "\"2021-12-06T13:30:45+09:00\""
        val decoded = Json.decodeFromString(serializer, src)

        val expected = OffsetDateTime.of(2021, 12, 6, 13, 30, 45, 0, ZoneOffset.ofHours(9)).toInstant()
        assertEquals(expected, decoded)

        val encoded = Json.encodeToString(serializer, expected)
        assertEquals(src, encoded)
    }
}