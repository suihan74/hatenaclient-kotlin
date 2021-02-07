package com.suihan74.hatena.bookmark.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantBookmarksEntryTimestampSerializerTest {
    @Test
    fun encode() {
        val serializer = BookmarksEntryTimestampSerializer()
        val src = "\"2021/12/06 13:30\""
        val decoded = Json.decodeFromString(serializer, src)

        val expected = OffsetDateTime.of(2021, 12, 6, 13, 30, 0, 0, ZoneOffset.ofHours(9)).toInstant()
        assertEquals(expected, decoded)

        val encoded = Json.encodeToString(serializer, expected)
        assertEquals(src, encoded)
    }
}