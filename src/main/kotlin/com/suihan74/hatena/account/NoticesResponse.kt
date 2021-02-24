package com.suihan74.hatena.account

import com.suihan74.hatena.serializer.EpochTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class NoticesResponse(
    val status : String,

    @SerialName("last_seen")
    @Serializable(with = EpochTimeSerializer::class)
    val lastSeen : Instant,

    val notices : List<Notice>
)
