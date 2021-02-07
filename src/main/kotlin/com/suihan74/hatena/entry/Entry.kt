package com.suihan74.hatena.entry

import com.suihan74.hatena.api.HatenaClientBase
import com.suihan74.hatena.bookmark.BookmarkResult
import com.suihan74.hatena.bookmark.serializer.InstantISO8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.time.Instant

/**
 * エントリ情報
 */
@Serializable
data class Entry(
    val eid : Long,

    @SerialName("title")
    private val _title : String,

    val description : String,

    val count : Int,

    val url : String,

    // 常にnull
    @SerialName("entry_url")
    private val _entryUrl : String? = null,

    @SerialName("root_url")
    private val _rootUrl : String? = null,

    @SerialName("favicon_url")
    private val _faviconUrl : String? = null,

    @SerialName("image")
    val imageUrl : String = "https://b.st-hatena.com/images/v4/public/common/noimage.png",

    @SerialName("image_l")
    val largeImage : ImageInfo? = null,

    @SerialName("amp_url")
    val ampUrl : String? = null,

    // ユーザーがブクマしている場合のみ取得
    @SerialName("bookmarked_data")
    val bookmarkedData : BookmarkResult? = null,

    // ホットエントリにのみ含まれる情報
    @SerialName("myhotentry_comments")
    val myHotEntryComments : List<BookmarkResult>? = null,

    @Serializable(with = InstantISO8601Serializer::class)
    val date: Instant? = null
) {
    val title : String by lazy {
        _title.indexOfFirst { it == '\n' }.let {
            if (it < 0) {
                _title
            }
            else {
                _title.substring(0 until it)
            }
        }
    }

    val entryUrl : String by lazy {
        _entryUrl ?: buildString {
            append(HatenaClientBase.baseUrlB, "entry/")
            val schemeHttps = "https://"
            if (url.startsWith(schemeHttps)) append("s/", url.substring(schemeHttps.length))
            else append(url.substring(schemeHttps.length - 1))
        }
    }

    val rootUrl : String by lazy {
        if (_rootUrl.isNullOrBlank()) {
            val uri = URI.create(url)
            val scheme = uri.scheme
            val authority = uri.authority

            if (scheme != null && authority != null) "$scheme://$authority/" else ""
        }
        else _rootUrl
    }

    val faviconUrl : String by lazy {
        _faviconUrl ?: let {
            "https://www.google.com/s2/favicons?domain=${URI.create(url).host}"
        }
    }

    // ------ //

    @Serializable
    data class ImageInfo(
        val url : String,

        val width : Int,

        val height : Int
    )
}