package com.suihan74.hatena.model.account

import com.suihan74.hatena.serializer.EpochTimeSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Tag(
    val index : Int,

    val count : Int,

    @Serializable(with = EpochTimeSerializer::class)
    val timestamp : Instant,

    val text : String = ""
)

// ------ //

@Serializable
data class TagsResponse(
    val count : Int,

    val status : Int,

    val tags : Map<String, Tag>
)
