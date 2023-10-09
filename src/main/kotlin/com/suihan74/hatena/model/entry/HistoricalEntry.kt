package com.suihan74.hatena.model.entry

import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.serializer.InstantISO8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class HistoricalEntry(
    val title : String,

    @SerialName("root_url")
    val rootUrl : String,

    @SerialName("canonical_url")
    val canonicalUrl : String,

    @SerialName("favicon_url")
    val faviconUrl : String,

    @SerialName("category_css_class_name")
    val categoryCssClassName : String,

    /** ユーザーではnull */
    val id : Long? = null,

    /** はてな全体ではnull */
    @SerialName("total_bookmarks")
    val totalBookmarks : Int? = null,

    /** ユーザーではnull */
    @SerialName("created_at")
    @Serializable(with = InstantISO8601Serializer::class)
    val createdAt : Instant? = null
) {
    fun toEntry(bookmarkedData: BookmarkResult? = null, count: Int? = null) : Entry = EntryItem(
        eid = id ?: 0L,
        title = title,
        description = "",
        count = totalBookmarks ?: count ?: 0,
        url = canonicalUrl,
        _rootUrl = rootUrl,
        _faviconUrl = faviconUrl,
        bookmarkedData = bookmarkedData,
        createdAt = createdAt ?: Instant.now()
    )
}

// ------ //

/** はてな全体の15周年タイムカプセルエントリ */
@Serializable
data class HatenaHistoricalEntry(
    val entries : List<HistoricalEntry>
)

// ------ //

/** ユーザーの15周年タイムカプセルエントリ */
@Serializable
data class UserHistoricalEntry(
    val entry : HistoricalEntry,

    @SerialName("created_at")
    @Serializable(with = InstantISO8601Serializer::class)
    val createdAt: Instant,

    @SerialName("comment_expanded")
    val commentExpanded : String,

    @SerialName("user_icon_url")
    val userIconUrl : String,
) {
    fun toEntry(user: String) = entry.toEntry(
        bookmarkedData = BookmarkResult(
            user = user,
            comment = commentExpanded,
            tags = emptyList(),
            timestamp = createdAt,
            userIconUrl = userIconUrl,
            commentRaw = commentExpanded,
            permalink = ""
        )
    )
}
