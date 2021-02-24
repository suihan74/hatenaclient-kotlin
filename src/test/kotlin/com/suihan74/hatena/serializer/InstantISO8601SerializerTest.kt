package com.suihan74.hatena.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantISO8601SerializerTest {
    /**
     * 正しくエンコード/デコード
     */
    @Test
    fun encode_decode() {
        val serializer = InstantISO8601Serializer()
        val src = "\"2021-12-06T13:30:45+09:00\""
        val decoded = Json.decodeFromString(serializer, src)

        val expected = OffsetDateTime.of(2021, 12, 6, 13, 30, 45, 0, ZoneOffset.ofHours(9)).toInstant()
        assertEquals(expected, decoded)

        val encoded = Json.encodeToString(serializer, expected)
        assertEquals(src, encoded)
    }

    /**
     * オフセットが必須
     */
    @Test
    fun missing_offset() {
        val serializer = InstantISO8601Serializer()
        val src = "\"2021-12-06T13:30:45\""
        runCatching {
            Json.decodeFromString(serializer, src)
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            fail()
        }
    }

    @Test
    fun invalid_date() {
        val serializer = InstantISO8601Serializer()

        // 年は4桁である必要がある。(紀元前はさらに頭に"-"がつく)
        runCatching {
            Json.decodeFromString(serializer, "\"193-01-01T00:00:00+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 13月
        runCatching {
            Json.decodeFromString(serializer, "\"2021-13-01T00:00:00+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 31日よりもあと
        runCatching {
            Json.decodeFromString(serializer, "\"2021-01-36T00:00:00+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 0月0日
        runCatching {
            Json.decodeFromString(serializer, "\"2021-00-00T13:30:45+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 24時間を超過
        runCatching {
            Json.decodeFromString(serializer, "\"2021-01-01T25:30:45+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 24時間を超過
        runCatching {
            Json.decodeFromString(serializer, "\"2021-01-01T24:30:45+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 60分を超過
        runCatching {
            Json.decodeFromString(serializer, "\"2021-01-01T00:60:00+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }

        // 60秒を超過
        runCatching {
            Json.decodeFromString(serializer, "\"2021-01-01T00:00:60+09:00\"")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            fail()
        }
    }
}