package com.suihan74.hatena.service

import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.HatenaClientBase
import com.suihan74.hatena.api.CertifiedEntryAPI
import com.suihan74.hatena.api.EntryAPI
import com.suihan74.hatena.exception.HatenaException
import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.extension.queryParameters
import com.suihan74.hatena.model.bookmark.BookmarkResult
import com.suihan74.hatena.model.entry.*
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

open class EntryService internal constructor(private val api: EntryAPI) {
    /**
     * カテゴリを指定して人気/新着エントリを取得する
     *
     * @param entriesType 人気or新着
     * @param category カテゴリ
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @param includeAMPUrls AMP用のURLを含める
     * @param includeBookmarkedData 自分のブクマ情報を含める(サインイン済みの場合)
     * @param includeBookmarksOfFollowings フォローしているユーザーのブクマ情報を含める(サインイン済みの場合)
     * @param includeAds はてなから提供される広告を含める
     * @return カテゴリの人気/新着エントリリスト
     * @throws HatenaException 通信失敗
     */
    suspend fun getEntries(
        entriesType: EntriesType,
        category: Category,
        limit: Int? = null,
        offset: Int? = null,
        includeAMPUrls: Boolean = true,
        includeBookmarkedData: Boolean = true,
        includeBookmarksOfFollowings: Boolean = true,
        includeAds: Boolean = false
    ) : List<EntryItem> =
        runCatching {
            api.getEntries(
                entriesType,
                category,
                limit,
                offset,
                includeAMPUrls,
                includeBookmarkedData,
                includeBookmarksOfFollowings,
                includeAds
            )
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * Issueを指定して人気/新着エントリを取得する
     *
     * @param entriesType 人気or新着
     * @param issue 特集
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @param includeAMPUrls AMP用のURLを含める
     * @param includeBookmarkedData 自分のブクマ情報を含める(サインイン済みの場合)
     * @param includeBookmarksByVisitor
     * @param includeBookmarksOfFollowings フォローしているユーザーのブクマ情報を含める(サインイン済みの場合)
     * @param includeAds はてなから提供される広告を含める
     * @return Issueの人気/新着エントリリスト
     * @throws HatenaException 通信失敗
     */
    suspend fun getIssueEntries(
        entriesType: EntriesType,
        issue: Issue,
        limit: Int? = null,
        offset: Int? = null,
        includeAMPUrls: Boolean = true,
        includeBookmarkedData: Boolean = true,
        includeBookmarksByVisitor: Boolean = true,
        includeBookmarksOfFollowings: Boolean = true,
        includeAds: Boolean = false
    ) : IssueEntriesResponse =
        runCatching {
            api.getIssueEntries(
                entriesType,
                issue,
                limit,
                offset,
                includeAMPUrls,
                includeBookmarkedData,
                includeBookmarksOfFollowings,
                includeAds
            )
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 指定カテゴリの特集一覧を取得する
     *
     * @return 指定カテゴリのIssueリスト
     * @throws HatenaException 通信失敗
     */
    suspend fun getIssues(category: Category) : IssuesResponse =
        runCatching {
            api.getIssues(category)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * エントリをクエリ検索する
     *
     * @return 検索結果のエントリリスト
     * @throws HatenaException 通信失敗
     */
    suspend fun searchEntries(
        searchType: SearchType,
        query: String,
        sortType: EntriesType = EntriesType.RECENT,
        users: Int? = null,
        dateBegin: Instant? = null,
        dateEnd: Instant? = null,
        safe: Boolean = false,
        limit: Int? = null,
        offset: Int? = null,
        includeBookmarkedData: Boolean = true
    ) : List<EntryItem> =
        runCatching {
            api.searchEntries(
                searchType,
                query,
                sortType,
                users,
                dateBegin,
                dateEnd,
                safe,
                limit,
                offset,
                includeBookmarkedData
            )
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 関連エントリを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getRelatedEntries(
        url: String,
        ad: Boolean = false
    ) : RelatedEntriesResponse =
        runCatching {
            api.getRelatedEntries(url, ad)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 指定URLのエントリを取得する
     */
    suspend fun getEntry(url: String) : Entry =
        runCatching {
            val commentPageUrl = HatenaClient.getEntryUrl(url)
            getEntryImpl(commentPageUrl)
        }.getOrDefault(createDummyEntry(url))

    /**
     * 指定エントリIDのエントリを取得する
     */
    suspend fun getEntry(eid: Long) : Entry =
        runCatching {
            val commentPageUrl = "${HatenaClient.baseUrlB}entry/$eid"
            getEntryImpl(commentPageUrl)
        }.getOrDefault(createDummyEntry("${HatenaClient.baseUrlB}entry/$eid")) // TODO

    /**
     * HTMLからエントリ情報を取得する
     */
    private suspend fun getEntryImpl(commentPageUrl: String) : Entry {
        return HatenaClient.generalService.getHtml(commentPageUrl) { html ->
            val root = html.getElementsByTag("html").first()!!
            val eid = root.attr("data-entry-eid").toLong()
            val count = root.attr("data-bookmark-count").toInt()
            val entryUrl = root.attr("data-entry-url")
            val createdAt =
                LocalDateTime.from(
                    DateTimeFormatter.ISO_ZONED_DATE_TIME
                        .parse(root.attr("data-entry-created"))
                ).toInstant(ZoneOffset.ofHours(9))
            val imageUrl = html.head().getElementsByTag("meta")
                .firstOrNull { it.attr("property") == "og:image" || it.attr("name") == "twitter:image:src" }
                ?.attr("content")
                ?: ""
            val title = html.getElementsByClass("entry-info-title").firstOrNull()?.text() ?: entryUrl
            val domainElement = html.getElementsByAttributeValue("data-gtm-label", "entry-info-domain").firstOrNull()
            val rootUrl = domainElement?.text()
            val faviconUrl = domainElement?.getElementsByTag("img")?.firstOrNull()?.attr("src")
            val description = html.getElementsByClass("entry-about-description").firstOrNull()?.text() ?: ""

            val bookmark =
                if (api is CertifiedEntryAPI) getBookmark(eid = eid, user = api.accountName)
                else null

            EntryItem(
                eid = eid,
                title = title,
                description = description,
                count = count,
                url = entryUrl,
                _rootUrl = rootUrl,
                _faviconUrl = faviconUrl,
                _imageUrl = imageUrl,
                createdAt = createdAt,
                bookmarkedData = bookmark
            )
        }
    }

    /**
     * ブクマが付いていないページのエントリ情報を捏造する
     */
    private suspend fun createDummyEntry(url: String) : Entry {
        return runCatching {
            HatenaClient.generalService.getHtmlDetectedCharset(url) { doc ->
                val isCommentPage =
                    Regex("""^${HatenaClient.baseUrlB}entry/\d+/comment/\S+$""")
                        .matches(url)
                val allElements = doc.allElements

                val pageTitle =
                    doc.select("title").html().let { title ->
                        if (title.isNullOrEmpty()) url else title
                    }

                val title =
                    if (isCommentPage) pageTitle
                    else allElements.firstOrNull { it.tagName() == "meta" && it.attr("property") == "og:title" }
                        ?.attr("content")
                        ?: pageTitle

                val description =
                    allElements.firstOrNull { it.tagName() == "meta" && it.attr("property") == "og:description" }
                        ?.attr("content")
                        ?: ""

                val actualUrl =
                    if (isCommentPage) url
                    else allElements.firstOrNull { it.tagName() == "meta" && it.attr("property") == "og:url" }
                        ?.attr("content")
                        ?: url

                val imageUrl =
                    allElements.firstOrNull { it.tagName() == "meta" && it.attr("property") == "og:image" }
                        ?.attr("content")
                        ?: ""

                EntryItem(
                    eid = 0L,
                    title = title,
                    description = description,
                    count = 0,
                    url = actualUrl,
                    createdAt = Instant.now(),
                    _imageUrl = imageUrl
                )
            }
        }.getOrElse {
            EntryItem(
                eid = 0,
                title = "",
                description = "",
                count = 0,
                createdAt = Instant.MIN,
                url = url,
                _rootUrl = null,
                _faviconUrl = null,
                _imageUrl = null
            )
        }
    }

    // ------ //

    /**
     * 指定サイトのエントリリストを取得する
     */
    suspend fun getSiteEntries(
        url: String,
        entriesType: EntriesType,
        all: Boolean = false,
        page: Int = 1
    ) : List<Entry> {
        val sort = when (entriesType) {
            EntriesType.RECENT -> if (all) "eid" else ""
            EntriesType.HOT -> "count"
        }
        val apiUrl = buildString {
            append(
                "${HatenaClient.baseUrlB}entrylist?url=${URLEncoder.encode(url, "UTF-8")}",
                "&page=$page",
                "&sort=$sort"
            )
        }

        return HatenaClient.generalService.getHtml(apiUrl) { html ->
            val anondRootUrl = "https://anond.hatelabo.jp/"
            val anondImageUrl = "https://cdn-ak-scissors.b.st-hatena.com/image/square/abf4f339344e96f39ffb9c18856eca5d454e63f8/height=280;version=1;width=400/https%3A%2F%2Fanond.hatelabo.jp%2Fimages%2Fog-image-1500.gif"

            val countRegex = Regex("""(\d+)\s*users""")
            val thumbnailRegex = Regex("""background-image:url\('(.+)'\);""")
            val rootUrlRegex = Regex("""^/site/""")
            val classNamePrefix = "entrylist-contents"
            val dateTimeFormat = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")

            html.body().getElementsByClass("$classNamePrefix-main").mapNotNull m@ { entry ->
                val (title, entryUrl, eid) = entry.getElementsByClass("$classNamePrefix-title").firstOrNull()?.let {
                    it.getElementsByTag("a").firstOrNull()?.let { link ->
                        Triple(
                            link.attr("title"),
                            link.attr("href"),
                            link.attr("data-entry-id").toLongOrNull()
                        )
                    }
                } ?: return@m null

                val count = entry.getElementsByClass("$classNamePrefix-users").firstOrNull()?.let {
                    countRegex.find(it.wholeText())?.groupValues?.get(1)?.toIntOrNull()
                } ?: return@m null

                val scheme =
                    if (entryUrl.startsWith("https://")) "https://"
                    else "http://"

                val rootUrl =
                    entry.getElementsByAttributeValue("data-gtm-click-label", "entry-info-root-url")
                        .firstOrNull()
                        ?.attr("href")
                        ?.let { path -> URLDecoder.decode(rootUrlRegex.replaceFirst(path, scheme), "UTF-8") }
                        ?: entryUrl

                val faviconUrl = entry.getElementsByClass("favicon").firstOrNull()?.attr("src") ?: ""

                val (description, imageUrl) = entry.getElementsByClass("$classNamePrefix-body").firstOrNull()?.let {
                    val description = it.wholeText() ?: ""
                    val imageUrl = it.getElementsByAttributeValue("data-gtm-click-label", "entry-info-thumbnail").firstOrNull()?.attr("style")?.let { style ->
                        thumbnailRegex.find(style)?.groupValues?.get(1)
                    } ?: if (rootUrl == anondRootUrl) anondImageUrl else ""

                    description to imageUrl
                } ?: ("" to "")

                val date = entry.getElementsByClass("$classNamePrefix-date").firstOrNull()?.let {
                    val text = it.wholeText() ?: return@let null
                    runCatching {
                        LocalDateTime.parse(text, dateTimeFormat).toInstant(ZoneOffset.ofHours(9))
                    }.getOrNull()
                } ?: return@m null

                EntryItem(
                    eid = eid ?: 0L,
                    title = title,
                    description = description,
                    count = count,
                    url = entryUrl,
                    _rootUrl = rootUrl,
                    _faviconUrl = faviconUrl,
                    _imageUrl = imageUrl,
                    createdAt = date
                )
            }
        }
    }

    // ------ //

    /**
     * 15周年記念時の「タイムカプセル」カテゴリのエントリを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getHistoricalEntries(year: Int) : List<Entry> =
        runCatching {
            val entries = api.__getHistoricalEntries(year).entries
            val bookmarksCounts = HatenaClient.bookmark.getBookmarksCount(entries.map { it.canonicalUrl })
            entries.map { entry ->
                entry.toEntry(count = bookmarksCounts.getOrDefault(entry.canonicalUrl, 0))
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 指定ユーザーがブクマしたエントリを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getBookmarkedEntries(
        user: String,
        tag: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ) : List<UserEntry> =
        runCatching {
            val response = api.__getBookmarkedEntries(
                user,
                tag,
                limit,
                offset
            )
            response.bookmarks
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 与えられたページのfaviconのURLを取得する
     *
     * @return faviconのURL(実際にはてながキャッシュしていて画像が取得できるかは考慮しない)
     */
    fun getFaviconUrl(url: String) : String =
        "https://cdn-ak2.favicon.st-hatena.com/?url=${URLEncoder.encode(url, "UTF-8")}"


    /**
     * 指定ページのエントリIDを取得する
     *
     * @throws HatenaException 通信失敗 / レスポンスの処理に失敗
     */
    suspend fun getEntryId(url: String) : Long =
        runCatching {
            val entryUrl = HatenaClient.getEntryUrl(url)
            HatenaClient.generalService.getHtml(entryUrl) { html ->
                html.getElementsByTag("html")
                    .first()!!
                    .attr("data-entry-eid")
                    .toLong()
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * エントリIDから対象のページのURLを取得する
     *
     * @param eid エントリID
     * @return 対象ページのURL
     * @throws HatenaException 通信失敗
     */
    suspend fun getUrl(eid: Long) : String =
        runCatching {
            val baseUrl = HatenaClient.baseUrlB
            val eidEntryUrl = buildString { append(baseUrl, "entry/", eid) }
            HatenaClient.generalService.get(eidEntryUrl).let { response ->
                if (!response.isSuccessful) throw HttpException(response)
                val entryUrl = response.raw().request.url.toString()
                val headHttps = "${baseUrl}entry/s/"
                val isHttps = entryUrl.startsWith(headHttps)
                val scheme =
                    if (isHttps) "https://"
                    else "http://"
                val tail = entryUrl.substring(
                    if (isHttps) headHttps.length
                    else headHttps.length - 2
                )

                "$scheme$tail"
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * エントリURLから対象ページのURLを取得する
     *
     * @param entryUrl エントリページのURL
     * @return エントリの対象となっている元ページのURL
     * @throws IllegalArgumentException 渡されたurlがエントリURLとして判別不可能
     *
     * cases
     * 1) https://b.hatena.ne.jp/entry/s/www.hoge.com/ ==> https://www.hoge.com/
     * 2) https://b.hatena.ne.jp/entry/https://www.hoge.com/ ==> https://www.hoge.com/
     * 3) https://b.hatena.ne.jp/entry/{eid}/comment/{username} ==> https://b.hatena.ne.jp/entry/{eid}  (modifySpecificUrls()を参照)
     * 4) https://b.hatena.ne.jp/entry?url=https~~~
     * 5) https://b.hatena.ne.jp/entry?eid=1234
     * 6) https://b.hatena.ne.jp/entry/{eid}
     * 7) https://b.hatena.ne.jp/entry.touch/s/~~~
     * 8) https://b.hatena.ne.jp/entry/panel/?url=~~~
     */
    fun getUrl(entryUrl: String) : String {
        val baseUrl = HatenaClientBase.baseUrlB
        if (entryUrl.startsWith("${baseUrl}entry?url=") || entryUrl.startsWith("${baseUrl}entry/panel/?url=")) {
            // 4, 8)
            return URI.create(entryUrl).queryParameters["url"] ?: throw IllegalArgumentException("invalid comment page url: $entryUrl")
        }
        else if (entryUrl.startsWith("${baseUrl}entry?eid=")) {
            // 5)
            val eid = URI.create(entryUrl).queryParameters["eid"] ?: throw IllegalArgumentException("invalid comment page url: $entryUrl")
            return "${baseUrl}entry/$eid"
        }

        val commentUrlRegex = Regex("""https?://b\.hatena\.ne\.jp/entry/(\d+)(/comment/\w+)?""")
        val commentUrlMatch = commentUrlRegex.matchEntire(entryUrl)
        if (commentUrlMatch != null) {
            // 3, 6)
            return "${baseUrl}entry/${commentUrlMatch.groups[1]!!.value}"
        }

        val regex = Regex("""https?://b\.hatena\.ne\.jp/entry(\.touch)?/(https://|s/)?(.+)""")
        val matches = regex.matchEntire(entryUrl) ?: throw IllegalArgumentException("invalid comment page url: $entryUrl")
        val path = matches.groups[3]?.value ?: throw IllegalArgumentException("invalid comment page url: $entryUrl")

        // 1,2)
        return if (matches.groups[2]?.value.isNullOrEmpty()) {
            if (path.startsWith("http://")) path // 2)
            else "http://$path" // 1)
        }
        else "https://$path"
    }

    /**
     * 対象ページのURLからエントリページのURLを取得する
     *
     * @param url エントリの対象となっている元ページのURL
     * @return エントリページのURL
     * @throws IllegalArgumentException "http"or"https"スキーム以外の文字列が渡された場合
     */
    fun getEntryUrl(url: String) : String = buildString {
        append("${HatenaClientBase.baseUrlB}entry/")
        append(
            when {
                url.startsWith("https://") -> "s/${url.substring("https://".length)}"
                url.startsWith("http://") -> url.substring("http://".length)
                else -> throw IllegalArgumentException("invalid url: $url")
            }
        )
    }

    /**
     * 対象ページのタイトルを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getPageTitle(url: String) : String =
        runCatching {
            HatenaClient.generalService.getHtml(url) { doc ->
                val titleTag = doc.getElementsByTag("title").firstOrNull()
                titleTag?.wholeText().orEmpty()
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * メンテナンス情報を取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getMaintenanceEntries() : List<MaintenanceEntry> =
        runCatching {
            val url = "https://maintenance.hatena.ne.jp"
            HatenaClient.generalService.getHtml(url) { doc ->
                val brRegex = Regex("""<br\w*/?>""")
                doc.getElementsByTag("article").mapNotNull { article ->
                    val titleTag = article.getElementsByTag("h2").firstOrNull() ?: return@mapNotNull null
                    val title = titleTag.wholeText()
                    val resolved = title.contains("復旧済")
                    val id = titleTag.id()
                    if (id.isNullOrBlank()) return@mapNotNull null

                    val titleLinkTag = article.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
                    val link = titleLinkTag.attr("href")

                    val paragraphs = article.getElementsByTag("p")
                    val paragraph =
                        paragraphs.firstOrNull { !it.hasClass("sectionheader") } ?: return@mapNotNull null
                    val header = paragraphs.firstOrNull { it.hasClass("sectionheader") } ?: return@mapNotNull null
                    val createdAt = parseTimestamp(header, "timestamp") ?: return@mapNotNull null
                    val updatedAt = parseTimestamp(header, "timestamp updated") ?: createdAt

                    MaintenanceEntry(
                        id = id,
                        title = title,
                        body = paragraph.html().replace(brRegex, "\n"),
                        resolved = resolved,
                        url = url + link,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                }
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    private fun parseTimestamp(header: Element, className: String) : Instant? =
        header.getElementsByClass(className).firstOrNull()?.let {
            val str = it.getElementsByTag("time").firstOrNull()?.wholeText() ?: return@let null
            val dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss")
            OffsetDateTime.of(
                LocalDateTime.parse(str, dateTimeFormatter),
                ZoneOffset.ofHours(9)
            ).toInstant()
        }

    // ------ //

    /**
     * エントリに指定ユーザーがつけたブコメを取得する
     */
    internal open suspend fun getBookmark(
        eid: Long,
        user: String
    ) : BookmarkResult? {
        return getBookmarkImpl(
            url = "${HatenaClient.baseUrlB}entry/$eid/comment/$user",
            eid = eid,
            user = user,
            generalService = HatenaClient.generalService
        )
    }
}

// ------ //

class CertifiedEntryService internal constructor(private val api: CertifiedEntryAPI) : EntryService(api) {
    /**
     * サインインユーザーがブクマしたエントリ一覧を取得する
     *
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @throws HatenaException 通信失敗
     */
    suspend fun getBookmarkedEntries(
        limit: Int? = null,
        offset: Int? = null
    ): List<EntryItem> =
        runCatching {
            api.getBookmarkedEntries(limit, offset)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * サインインユーザーがブクマしたエントリを検索する
     *
     * @param searchType 検索モード
     * @param query 検索文字列
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @throws HatenaException 通信失敗
     */
    suspend fun searchBookmarkedEntries(
        searchType: SearchType,
        query: String,
        limit: Int? = null,
        offset: Int? = null
    ): List<EntryItem> =
        runCatching {
            api.searchBookmarkedEntries(searchType, query, limit, offset)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * マイホットエントリを取得する
     *
     * @param date 取得する日付
     * @param includeAMPUrls ampのURLを含めるか否か
     * @throws HatenaException 通信失敗
     */
    suspend fun getMyHotEntries(
        date: String? = null,
        includeAMPUrls: Boolean = true
    ): List<MyHotEntry> =
        runCatching {
            api.getMyHotEntries(date, includeAMPUrls)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * ユーザーの15周年タイムカプセルエントリを取得する
     *
     * @param year 2005 ~ 2020
     * @throws HatenaException 通信失敗
     */
    suspend fun getUserHistoricalEntries(year: Int, limit: Int = 10): List<Entry> =
        runCatching {
            api.__getUserHistoricalEntries(year, limit).map {
                it.toEntry(user = api.accountName)
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * フォロー中ユーザーがブクマしたエントリを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getFollowingEntries(
        includeAmpUrls: Boolean = true,
        limit: Int? = null,
        offset: Int? = null
    ): List<Entry> =
        runCatching {
            api.__getFollowingEntries(includeAmpUrls, limit, offset).bookmarks
                .groupBy { it.eid }
                .values
                .map { items ->
                    val bookmarks = buildList {
                        for (item in items) {
                            add(item.bookmark)
                        }
                    }
                    val item = items[0]
                    EntryItem(
                        title = item.entry.title,
                        url = item.entry.url,
                        eid = item.eid,
                        description = item.entry.content,
                        count = item.entry.count,
                        createdAt = item.entry.createdAt,
                        _entryUrl = HatenaClient.getEntryUrl(item.entry.url),
                        _rootUrl = null,
                        _faviconUrl = item.entry.faviconUrl,
                        _imageUrl = item.entry.imageUrl,
                        bookmarksOfFollowings = bookmarks,
                    )
                }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * エントリに指定ユーザーがつけたブコメを取得する
     */
    override suspend fun getBookmark(
        eid: Long,
        user: String
    ) : BookmarkResult? {
        return getBookmarkImpl(
            url = "${HatenaClient.baseUrlB}entry/$eid/comment/$user",
            eid = eid,
            user = user,
            generalService = api.generalService
        )
    }
}