package com.suihan74.hatena.bookmark

import com.suihan74.hatena.entry.Entry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 人気コメントを取得するためのレスポンス
 */
@Serializable
data class BookmarksDigest (
    @SerialName("refered_blog_entries")
    val referredBlogEntries : List<Entry>,

    @SerialName("scored_bookmarks")
    val scoredBookmarks : List<Bookmark>,

    @SerialName("favorite_bookmarks")
    val favoriteBookmarks : List<Bookmark>
)
