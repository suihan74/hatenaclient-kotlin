package com.suihan74.hatena.bookmark.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * ISO 8601規格の日時文字列を`Instant`に変換するシリアライザ
 */
internal open class InstantISO8601Serializer : KSerializer<Instant> {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX")

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.atZone(ZoneId.systemDefault()).format(formatter))
    }

    override fun deserialize(decoder: Decoder): Instant {
        return ZonedDateTime.parse(decoder.decodeString(), formatter).toInstant()
    }
}

// ------ //

/**
 * `BookmarksEntry.Bookmark`用のtimestampシリアライザ
 *
 * タイムゾーンが渡されてこないので，暗黙的に"Asia/Tokyo"として処理する
 */
internal class BookmarksEntryTimestampSerializer : KSerializer<Instant> {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")
    private val zoneId = ZoneId.of("Asia/Tokyo")

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(formatter.format(value.atZone(zoneId)))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val localDateTime = LocalDateTime.parse(decoder.decodeString(), formatter)
        return ZonedDateTime.of(localDateTime, zoneId).toInstant()
    }
}

// ------ //

/**
 * エポックタイム値とInstantを変換するためのシリアライザ
 */
internal class EpochTimeSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSecond)
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochSecond(decoder.decodeLong())
    }
}