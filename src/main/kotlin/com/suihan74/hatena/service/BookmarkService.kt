package com.suihan74.hatena.service

import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.api.BookmarkAPI
import com.suihan74.hatena.api.CertifiedBookmarkAPI
import com.suihan74.hatena.exception.HatenaException
import com.suihan74.hatena.model.bookmark.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

open class BookmarkService internal constructor(private val api: BookmarkAPI) {
    /**
     * 新着ブクマリストを取得する
     *
     * @param url ブクマを取得する対象ページURL
     * @param limit 一度に取得するブクマ件数の上限
     * @param cursor 取得開始位置を指定するカーソル
     * @return 新着ブクマリスト
     * @throws HatenaException 通信失敗
     */
    suspend fun getRecentBookmarks(
        url: String,
        limit: Int? = null,
        cursor: String? = null
    ) : BookmarksResponse =
        runCatching {
            api.getRecentBookmarks(url, limit, cursor)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 人気ブクマリストを取得する
     *
     * @param url ブクマを取得する対象ページURL
     * @param limit 一度に取得するブクマ件数の上限
     * @return ダイジェストブクマ情報
     * @throws HatenaException 通信失敗
     */
    suspend fun getBookmarksDigest(
        url: String,
        limit: Int? = null
    ) : BookmarksDigest =
        runCatching {
            api.getBookmarksDigest(url, limit)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * ページに対する全ブクマ情報を内包するエントリ情報を取得する
     *
     * @return ブクマ情報をもつエントリ情報
     * @throws HatenaException 通信失敗
     */
    suspend fun getBookmarksEntry(url: String) : BookmarksEntry =
        runCatching {
            api.getBookmarksEntry(url)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 対象URLについたブクマ数を取得する
     *
     * @return `{"url": count}`のマップ
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getBookmarksCount(urls: List<String>) : Map<String, Int> = coroutineScope {
        val windowSize = 50
        val tasks = urls
            .distinct()
            .windowed(size = windowSize, step = windowSize, partialWindows = true)
            .map {
                async {
                    runCatching {
                        api.getBookmarksCount(it)
                    }.getOrNull()
                }
            }
        tasks.awaitAll()

        return@coroutineScope buildMap<String, Int> {
            for (t in tasks) {
                t.getCompleted()?.let {
                    this.putAll(it)
                }
            }
        }
    }

    /**
     * 対象URLについたブクマ数を取得する
     *
     * @return ブクマ数
     * @throws HatenaException 通信失敗
     */
    suspend fun getBookmarksCount(url: String) : Int =
        runCatching {
            val map = api.getBookmarksCount(listOf(url))
            map[url] ?: 0
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * ユーザーのツイートとそのクリック数を取得する(ユーザー固定、URL複数)
     *
     * @param user 対象ユーザー
     * @param urls 対象URL
     * @return ブクマに紐づいたツイートのアドレスとそのクリック数．count=0の項目は含まれない
     * @throws HatenaException 通信失敗
     */
    suspend fun getTweetsAndClicks(user: String, urls: List<String>) : List<TweetsAndClicks> =
        runCatching {
            if (urls.isEmpty()) {
                return emptyList()
            }
            val requestBody = TweetsAndClicksRequestBody(
                urls.map { TweetsAndClicksRequestBodyItem(url = it, user = user) }
            )
            api.getTweetsAndClicks(requestBody)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * ユーザーのツイートとそのクリック数を取得する(ユーザー複数、URL固定)
     *
     * @param users 対象ユーザー
     * @param url 対象URL
     * @return ブクマに紐づいたツイートのアドレスとそのクリック数．count=0の項目は含まれない
     * @throws HatenaException 通信失敗
     */
    suspend fun getTweetsAndClicks(users: List<String>, url: String) : List<TweetsAndClicks> =
        runCatching {
            if (users.isEmpty()) {
                return emptyList()
            }
            val requestBody = TweetsAndClicksRequestBody(
                users.map { TweetsAndClicksRequestBodyItem(url = url, user = it) }
            )
            api.getTweetsAndClicks(requestBody)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * エントリに指定ユーザーがつけたブコメを取得する
     *
     * @return ブクマ情報
     * @throws HatenaException 通信失敗
     */
    open suspend fun getBookmark(eid: Long, user: String) : BookmarkResult? =
        runCatching {
            getBookmarkImpl(
                url = "${HatenaClient.baseUrlB}entry/$eid/comment/$user",
                eid = eid,
                user = user,
                generalService = HatenaClient.generalService
            )
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrNull()
}

// ------ //

class CertifiedBookmarkService internal constructor(private val api: CertifiedBookmarkAPI) : BookmarkService(api) {
    /**
     * ブックマークを投稿する
     *
     * @param url ブクマするURL
     * @param comment ブックマークコメント(UTF-8日本語100文字以内/タグ部分除く)
     * @param postTwitter Twitterに連携投稿する
     * @param postFacebook Facebookに連携投稿する
     * @param readLater 「あとで読む」
     * @param private 非公開ブクマ
     *
     * @return 登録完了したブクマ情報
     * @throws HatenaException 通信失敗
     */
    suspend fun postBookmark(
        url: String,
        comment: String = "",
        postTwitter: Boolean = false,
        postFacebook: Boolean = false,
        readLater: Boolean = false,
        private: Boolean = false
    ) : BookmarkResult =
        runCatching {
            api.postBookmark(
                url = url,
                comment = comment,
                postTwitter = postTwitter,
                postFacebook = postFacebook,
                readLater = readLater,
                private = private
            )
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * ブックマークを削除する
     */
    suspend fun deleteBookmark(url: String) : Unit =
        runCatching {
            api.deleteBookmark(url)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * エントリに指定ユーザーがつけたブコメを取得する
     *
     * @return ブクマ情報
     * @throws HatenaException 通信失敗
     */
    override suspend fun getBookmark(eid: Long, user: String) : BookmarkResult? =
        runCatching {
            getBookmarkImpl(
                url = "${HatenaClient.baseUrlB}entry/$eid/comment/$user",
                eid = eid,
                user = user,
                generalService = api.generalService
            )
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrNull()
}