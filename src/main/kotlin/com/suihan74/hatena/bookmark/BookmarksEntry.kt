package com.suihan74.hatena.bookmark

import com.suihan74.hatena.serializer.BookmarksEntryTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class BookmarksEntry (
    @SerialName("eid")
    val id : Long,

    val title : String,

    val count : Int,

    val url : String,

    @SerialName("entry_url")
    val entryUrl : String,

    @SerialName("requested_url")
    val requestedUrl : String,

    val screenshot : String,

    val bookmarks : List<Bookmark>
) {
    /**
     * ブクマした全ユーザーが付けたタグをその数と共に集計して返す
     */
    val tags : List<Pair<String, Int>> by lazy {
        bookmarks.flatMap { it.tags }
            .groupBy { it }
            .map { it.key to it.value.count() }
            .sortedByDescending { it.second }
    }

    // ------ //

    @Serializable
    data class Bookmark (
        val user : String,

        val comment : String,

        val tags : List<String>,

        @Serializable(with = BookmarksEntryTimestampSerializer::class)
        val timestamp : Instant
    ) {

        /*
        fun getTagsText(
            separator: CharSequence = ", ",
            prefix: CharSequence = "",
            postfix: CharSequence = "",
            limit: Int = -1,
            truncated: CharSequence = "...",
            transform: ((String)->CharSequence)? = null
        ) = tags.joinToString(separator, prefix, postfix, limit, truncated, transform)

        // ブックマーク自身を指すURLを取得する
        fun getBookmarkUrl(entry: Entry) : String {
            val dateFormat = DateTimeFormatter.ofPattern("uuuuMMdd")
            val date = timestamp.format(dateFormat)
            return "${HatenaClient.B_BASE_URL}/$user/$date#bookmark-${entry.id}"
        }

        /** タグを含んだコメントを取得する */
        val commentRaw : String get() =
            getTagsText(separator = "") { "[$it]" } + comment

        /** ブコメの中身が更新されていないかを確認する */
        fun same(other: Bookmark?) : Boolean {
            if (other == null) return false
            val starCount = starCount ?: emptyList()
            val otherStarCount = other.starCount ?: emptyList()

            return user == other.user &&
                commentRaw == other.commentRaw &&
                timestamp == other.timestamp &&
                compareStarCount(starCount, otherStarCount) &&
                compareStarCount(otherStarCount, starCount)
        }

        private fun compareStarCount(starCount: List<Star>, other: List<Star>) : Boolean =
            starCount.all { i ->
                other.firstOrNull { o -> o.user == i.user && o.color == i.color }?.count == i.count
            }
         */
    }
}
