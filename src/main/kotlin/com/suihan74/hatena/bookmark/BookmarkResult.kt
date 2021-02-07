package com.suihan74.hatena.bookmark

import com.suihan74.hatena.serializer.BooleanAsBinarySerializer
import com.suihan74.hatena.bookmark.serializer.EpochTimeSerializer
import com.suihan74.hatena.star.Star
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * ブクマした際に返ってくる結果 + マイホットエントリコメント
 */
@Serializable
data class BookmarkResult (
    val user : String,

    val comment : String,

    val tags : List<String>,

    @SerialName("epoch")
    @Serializable(with = EpochTimeSerializer::class)
    val timestamp : Instant,

    @SerialName("timestamp")
    private val _timestamp : String,

    @SerialName("user_icon_url")
    val userIconUrl : String,

    @SerialName("comment_raw")
    val commentRaw : String,  // タグ文字列( [tag] )を含むコメント

    val permalink : String,

    // 以下、ブクマリザルトとして扱われる場合のみ含まれる

    @Serializable(with = BooleanAsBinarySerializer::class)
    val success : Boolean? = null,

    @Serializable(with = BooleanAsBinarySerializer::class)
    val private : Boolean? = null,

    val eid : Long? = null,

    @SerialName("star_count")
    val starsCount : List<Star>? = null
)
