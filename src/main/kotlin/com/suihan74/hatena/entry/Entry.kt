package com.suihan74.hatena.entry

import com.suihan74.hatena.api.HatenaClient
import com.suihan74.hatena.bookmark.BookmarkResult
import com.suihan74.hatena.extension.toUserIconUrl
import com.suihan74.hatena.serializer.BooleanAsBinarySerializer
import com.suihan74.hatena.serializer.InstantISO8601Serializer
import com.suihan74.hatena.star.StarCount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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

    abstract val bookmarksOfFollowings : List<BookmarkResult>

    // ユーザーがブクマしている場合のみ取得
    abstract val bookmarkedData : BookmarkResult?

    abstract val isPr : Boolean

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
        _faviconUrl ?: (TEMP_FAVICON_URL_BASE + URI.create(url).host)
    }

    // ------ //

    companion object {
        const val IMAGE_URL_NO_IMAGE = "https://b.st-hatena.com/images/v4/public/common/noimage.png"
        const val TEMP_FAVICON_URL_BASE = "https://www.google.com/s2/favicons?domain="
    }
}

// ------ //

/**
 * エントリ情報(カテゴリ指定で得られるエントリなど)
 */
@SerialName("entry")
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

    @SerialName("bookmarks_of_followings")
    override val bookmarksOfFollowings : List<BookmarkResult> = emptyList(),

    // ユーザーがブクマしている場合のみ取得
    @SerialName("bookmarked_data")
    override val bookmarkedData : BookmarkResult? = null,

    @SerialName("image_l")
    val largeImage : ImageInfo? = null,

    // ホットエントリにのみ含まれる情報
    @SerialName("myhotentry_comments")
    val myHotEntryComments : List<BookmarkResult>? = null,

    @SerialName("is_pr")
    @Serializable(with = BooleanAsBinarySerializer::class)
    override val isPr : Boolean = false,

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
 * エントリ情報(特集指定で得られるエントリなど)
 */
@SerialName("issue_entry")
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

    @SerialName("bookmarks_of_followings")
    override val bookmarksOfFollowings : List<BookmarkResult> = emptyList(),

    // ユーザーがブクマしている場合のみ取得
    @SerialName("bookmarked_data")
    override val bookmarkedData : BookmarkResult? = null,

    @SerialName("is_pr")
    override val isPr : Boolean = false,

) : Entry()

// ------ //

/**
 * エントリ情報(マイホットエントリ用)
 */
@SerialName("my_hot_entry")
@Serializable
data class MyHotEntry(
    override val title : String,

    override val url : String,

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

    @SerialName("bookmarks_of_followings")
    override val bookmarksOfFollowings : List<BookmarkResult> = emptyList(),

    // ユーザーがブクマしている場合のみ取得
    @SerialName("bookmarked_data")
    override val bookmarkedData : BookmarkResult? = null,

    @SerialName("myhotentry_comments")
    val myHotEntryComments: List<BookmarkResult>? = null,

    @SerialName("is_pr")
    override val isPr : Boolean = false,

    // eidが無い場合がある

    @SerialName("eid")
    val _eid : Long? = null,

    @SerialName("entry_id")
    private val entry_id : Long

) : Entry() {
    @Transient
    override val eid: Long = entry_id
}

// ------ //

@SerialName("user_entry")
@Serializable
data class UserEntry(
    @SerialName("entry_id")
    override val eid : Long,

    val comment : UserEntryComment,

    val entry : UserEntryBody,

    @SerialName("user_name")
    val userName : String,

    // public/private
    val status : String,

    @SerialName("created_at")
    @Serializable(with = InstantISO8601Serializer::class)
    override val createdAt : Instant,
) : Entry() {
    override val url: String = entry.url

    override val title: String = entry.title

    override val count: Int = entry.totalBookmarks

    override val description: String = entry.content

    @SerialName("entry_url")
    override val _entryUrl : String? = null

    @SerialName("root_url")
    override val _rootUrl : String? = null

    @SerialName("favicon_url")
    override val _faviconUrl : String = entry.faviconUrl ?: ""

    @SerialName("image")
    override val _imageUrl : String? = entry.imageUrl

    @SerialName("amp_url")
    override val ampUrl : String? = null

    @SerialName("bookmarks_of_followings")
    override val bookmarksOfFollowings : List<BookmarkResult> = emptyList()

    @SerialName("bookmarked_data")
    override val bookmarkedData = BookmarkResult(
        user = userName,
        userIconUrl = userName.toUserIconUrl,
        comment = comment.body,
        commentRaw = comment.raw,
        commentExpanded = comment.body,
        tags = comment.tags,
        timestamp = createdAt,
        permalink = "https://b.hatena.ne.jp/entry/%d/comment/%s".format(eid, userName),
        success = true,
        private = status == "private",
        eid = eid
    )

    @SerialName("is_pr")
    override val isPr : Boolean = false
}