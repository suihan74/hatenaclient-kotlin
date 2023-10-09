package com.suihan74.hatena.service

import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.model.bookmark.BookmarkResult
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * ブクマ情報をHTMLから取得する
 */
internal suspend fun getBookmarkImpl(
    url: String,
    eid: Long,
    user: String,
    generalService: GeneralService
) : BookmarkResult? {
    // サインインしていないクライアントでも取得できればprivateではないと判断する
    val private =
        if (generalService != HatenaClient.generalService) {
            HatenaClient.generalService.get(url).code() != 200
        }
        else false

    return runCatching {
        generalService.getHtml(url) { doc ->
            // ブクマされていない or アクセスできない
            if (doc.getElementsByTag("html").first()!!.attr("data-page-scope") != "EntryComment") {
                return@getHtml null
            }

            doc.getElementsByClass("comment-body").first()?.let { body ->
                val comment = body.getElementsByClass("comment-body-text").text().orEmpty()
                val tags =
                    body.getElementsByClass("comment-body-tags").first()
                        ?.getElementsByTag("li")
                        ?.map { it.wholeText() }
                        ?: emptyList()
                val commentRaw = tags.joinToString(separator = "", postfix = comment) { "[$it]" }

                val dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")
                val timestampStr = body.getElementsByClass("comment-body-date").first()!!.wholeText()
                val timestamp =
                    OffsetDateTime.of(
                        LocalDateTime.parse(timestampStr, dateTimeFormatter),
                        ZoneOffset.ofHours(9)
                    ).toInstant()

                BookmarkResult(
                    user = user,
                    comment = comment,
                    tags = tags,
                    timestamp = timestamp,
                    userIconUrl = HatenaClient.user.getUserIconUrl(user),
                    commentRaw = commentRaw,
                    permalink = url,
                    eid = eid,
                    private = private
                )
            }
        }
    }.getOrNull()
}