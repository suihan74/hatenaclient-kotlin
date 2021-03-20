package com.suihan74.hatena.entry

import com.suihan74.hatena.serializer.InstantISO8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserEntryComment (
    val raw : String,
    val tags : List<String>,
    val body : String
)

@Serializable
data class UserEntryBody (
    val title : String,

    val url : String,

    val content : String,

    @SerialName("total_bookmarks")
    val totalBookmarks : Int,

    @SerialName("entry_id")
    val entryId : Long,

    @SerialName("created_at")
    @Serializable(with = InstantISO8601Serializer::class)
    val createdAt : Instant,

    @SerialName("image_url")
    val imageUrl : String? = null,

    @SerialName("favicon_url")
    val faviconUrl : String? = null,
)

@Serializable
data class UserEntryResponse (
    val bookmarks : List<UserEntry>
)
