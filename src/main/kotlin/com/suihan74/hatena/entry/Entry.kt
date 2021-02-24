package com.suihan74.hatena.entry

import com.suihan74.hatena.api.HatenaClient
import com.suihan74.hatena.bookmark.BookmarkResult
import com.suihan74.hatena.serializer.InstantISO8601Serializer
import com.suihan74.hatena.star.StarCount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.time.Instant

@Serializable
sealed class Entry {
    abstract val title : String

    abstract val url : String

    abstract val eid : Long

    abstract val description : String

    abstract val count : Int

    abstract val _entryUrl : String?

    abstract val _rootUrl : String?

    abstract val _faviconUrl : String?

    abstract val _imageUrl : String?

    abstract val ampUrl : String?

    // ユーザーがブクマしている場合のみ取得
    abstract val bookmarkedData : BookmarkResult?

    abstract val createdAt : Instant

    // --- //

    val entryUrl : String by lazy {
        _entryUrl ?: HatenaClient.getEntryUrl(url)
    }

    val rootUrl : String by lazy {
        _rootUrl ?: let {
            val uri = URI.create(url)
            val scheme = uri.scheme
            val authority = uri.authority

            if (scheme != null && authority != null) "$scheme://$authority/" else ""
        }
    }

    val imageUrl : String by lazy {
        _imageUrl ?: IMAGE_URL_NO_IMAGE
    }

    val faviconUrl : String by lazy {
        _faviconUrl ?: TEMP_FAVICON_URL_BASE + URI.create(url).host
    }

    // ------ //

    companion object {
        const val IMAGE_URL_NO_IMAGE = "https://b.st-hatena.com/images/v4/public/common/noimage.png"
        const val TEMP_FAVICON_URL_BASE = "https://www.google.com/s2/favicons?domain="
    }
}

// ------ //

/**
 * エントリ情報
 */
@SerialName("entries")
@Serializable
data class EntryItem(
    override val title : String,

    override val url : String,

    override val eid : Long,

    override val description : String,

    override val count : Int,

    @SerialName("date")
    @Serializable(with = InstantISO8601Serializer::class)
    override val createdAt : Instant,

    @SerialName("entry_url")
    override val _entryUrl : String? = null,

    @SerialName("root_url")
    override val _rootUrl : String? = null,

    @SerialName("favicon_url")
    override val _faviconUrl : String? = null,

    @SerialName("image")
    override val _imageUrl : String? = null,

    @SerialName("amp_url")
    override val ampUrl : String? = null,

    // ユーザーがブクマしている場合のみ取得
    @SerialName("bookmarked_data")
    override val bookmarkedData : BookmarkResult? = null,

    @SerialName("image_l")
    val largeImage : ImageInfo? = null,

    // ホットエントリにのみ含まれる情報
    @SerialName("myhotentry_comments")
    val myHotEntryComments : List<BookmarkResult>? = null,

    // BookmarkedEntriesに含まれる
    @SerialName("is_wiped")
    val isWiped : Boolean = false,

    // BookmarkedEntriesに含まれる
    @SerialName("star_count")
    val starCount: List<StarCount> = emptyList()

) : Entry() {
    @Serializable
    data class ImageInfo(
        val url : String,

        val width : Int,

        val height : Int
    )
}

// ------ //

/**
 * エントリ情報
 */
@SerialName("issue_entries")
@Serializable
data class IssueEntry(
    override val title : String,

    override val url : String,

    @SerialName("entry_id")
    override val eid : Long,

    @SerialName("content")
    override val description : String,

    @SerialName("total_bookmarks")
    override val count : Int,

    @SerialName("created_at")
    @Serializable(with = InstantISO8601Serializer::class)
    override val createdAt : Instant,

    @SerialName("entry_url")
    override val _entryUrl : String? = null,

    @SerialName("root_url")
    override val _rootUrl : String? = null,

    @SerialName("favicon_url")
    override val _faviconUrl : String? = null,

    @SerialName("image_url")
    override val _imageUrl : String? = null,

    @SerialName("amp_url")
    override val ampUrl : String? = null,

    // ユーザーがブクマしている場合のみ取得
    @SerialName("bookmarked_data")
    override val bookmarkedData : BookmarkResult? = null,

) : Entry()
