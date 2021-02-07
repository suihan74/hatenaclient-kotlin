package com.suihan74.hatena.star.serializer

import com.suihan74.hatena.star.StarColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

internal class StarColorSerializer : KSerializer<StarColor> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StarColor) {
        encoder.encodeString(value.name.toLowerCase(Locale.ENGLISH))
    }

    override fun deserialize(decoder: Decoder): StarColor {
        return StarColor.fromName(decoder.decodeString())
    }
}
