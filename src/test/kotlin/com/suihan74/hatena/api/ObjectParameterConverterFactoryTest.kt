package com.suihan74.hatena.api

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ObjectParameterConverterFactoryTest {
    @Test
    fun testInstantConverter() {
        val converter = ObjectParameterConverterFactory.InstantConverter
        val input = OffsetDateTime.of(2023, 1, 22, 1, 0, 0, 0, ZoneOffset.ofHours(9))
        val result = converter.convert(input.toInstant())
        assertEquals("2023-01-22", result)
    }

    @Test
    fun testInstantConverterUtc() {
        val converter = ObjectParameterConverterFactory.InstantConverter
        val input = OffsetDateTime.of(2023, 1, 21, 15, 0, 0, 0, ZoneOffset.ofHours(0))
        val result = converter.convert(input.toInstant())
        assertEquals("2023-01-22", result)
    }

    @Test
    fun testInstantConverterUtc2() {
        val converter = ObjectParameterConverterFactory.InstantConverter
        val input = OffsetDateTime.of(2023, 1, 21, 14, 59, 59, 999, ZoneOffset.ofHours(0))
        val result = converter.convert(input.toInstant())
        assertEquals("2023-01-21", result)
    }
}