package com.suihan74.hatena.account.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 真偽値を0/1で表現しているJsonを扱うためのシリアライザ
 */
class BooleanAsBinarySerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(
            if (value) 1 else 0
        )
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() != 0
    }
}