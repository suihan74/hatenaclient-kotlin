package com.suihan74.hatena.api

import com.suihan74.hatena.bookmark.*
import com.suihan74.hatena.exception.HttpException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.http.*

/**
 * ブックマーク関係のAPI
 */
interface BookmarkService {
    /**
     * 新着ブクマリストを取得する
     *
     * @param url ブクマを取得する対象ページURL
     * @param limit 一度に取得するブクマ件数の上限
     * @param cursor 取得開始位置を指定するカーソル
     */
    @GET("api/ipad.entry_bookmarks_with_cursor")
    suspend fun getRecentBookmarks(
        @Query("url") url: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ) : BookmarksResponse

    /**
     * 人気ブクマリストを取得する
     *
     * @param url ブクマを取得する対象ページURL
     * @param limit 一度に取得するブクマ件数の上限
     */
    @GET("api/ipad.entry_reactions")
    suspend fun getBookmarksDigest(
        @Query("url") url: String,
        @Query("limit") limit: Int? = null
    ) : BookmarksDigest

    /**
     * ページに対する全ブクマ情報を内包するエントリ情報
     */
    @GET("entry/jsonlite/")
    suspend fun getBookmarksEntry(
        @Query("url") url: String
    ) : BookmarksEntry

    // ------ //

    /**
     * 指定URLのブクマ数を取得する
     *
     * 最大50件まで一度の通信で取得できるので、50件ずつ別々のリクエストを送出するように拡張関数を用意する
     * @see BookmarkService.getBookmarksCount
     */
    @GET("https://bookmark.hatenaapis.com/count/entries")
    suspend fun __getBookmarksCount(
        @Query("url") urls: List<String>
    ) : Map<String, Int>

    /**
     * 指定URLに対するユーザーのブクマに紐づいたツイートとそのクリック数を取得する
     */
    @POST("api/internal/bookmarks/tweets_and_clicks")
    suspend fun __getTweetsAndClicks(
        @Body requestBody: TweetsAndClicksRequestBody
    ) : List<TweetsAndClicks>
}

/**
 * 対象URLについたブクマ数を取得する
 *
 * @return {"url": count} のマップ
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun BookmarkService.getBookmarksCount(urls: List<String>) : Map<String, Int> = coroutineScope {
    val windowSize = 50
    val tasks = urls
        .distinct()
        .windowed(size = windowSize, step = windowSize, partialWindows = true)
        .map {
            async { __getBookmarksCount(it) }
        }
    tasks.awaitAll()

    return@coroutineScope HashMap<String, Int>().also { result ->
        tasks.forEach { result.putAll(it.getCompleted()) }
    }
}

/**
 * 対象URLについたブクマ数を取得する
 *
 * @return ブクマ数
 */
suspend fun BookmarkService.getBookmarksCount(url: String) : Int {
    val map = __getBookmarksCount(listOf(url))
    return map[url] ?: 0
}

/**
 * ユーザーのツイートとそのクリック数を取得する(ユーザー固定、URL複数)
 *
 * @param user 対象ユーザー
 * @param urls 対象URL
 * @return ブクマに紐づいたツイートのアドレスとそのクリック数．count=0の項目は含まれない
 * @throws HttpException
 */
suspend fun BookmarkService.getTweetsAndClicks(
    user: String,
    urls: List<String>
) : List<TweetsAndClicks> {
    if (urls.isEmpty()) {
        return emptyList()
    }

    val requestBody = TweetsAndClicksRequestBody(
        urls.map { TweetsAndClicksRequestBodyItem(url = it, user = user) }
    )
    return __getTweetsAndClicks(requestBody)
}

/**
 * ユーザーのツイートとそのクリック数を取得する(ユーザー複数、URL固定)
 *
 * @param users 対象ユーザー
 * @param url 対象URL
 * @return ブクマに紐づいたツイートのアドレスとそのクリック数．count=0の項目は含まれない
 * @throws HttpException
 */
suspend fun BookmarkService.getTweetsAndClicks(
    users: List<String>,
    url: String
) : List<TweetsAndClicks> {
    if (users.isEmpty()) {
        return emptyList()
    }

    val requestBody = TweetsAndClicksRequestBody(
        users.map { TweetsAndClicksRequestBodyItem(url = url, user = it) }
    )
    return __getTweetsAndClicks(requestBody)
}

// ------ //

/**
 * 認証が必要なブックマーク関係API
 */
interface CertifiedBookmarkService : BookmarkService {
    val accountName : String

    val rks : String

    /**
     * ブックマークを投稿する
     *
     * 2021年時点でEvernoteは使用不可能
     *
     * @see CertifiedBookmarkService.postBookmark
     */
    @FormUrlEncoded
    @POST("{account}/add.edit.json")
    suspend fun __postBookmark(
        @Field("url") url: String,
        @Field("comment") comment: String = "",
        @Field("post_twitter") postTwitter: Boolean = false,
        @Field("post_facebook") postFacebook: Boolean = false,
        @Field("post_evernote") postEvernote: Boolean = false,
        @Field("read_later") readLater: Boolean = false,
        @Field("private") private: Boolean = false,
        @Path("account") accountName: String = this.accountName,
        @Field("rks") rks: String = this.rks
    ) : BookmarkResult

    /**
     * ブックマークを削除する
     */
    @FormUrlEncoded
    @POST("{account}/api.delete_bookmark.json")
    suspend fun deleteBookmark(
        @Field("url") url: String,
        @Path("account") accountName: String = this.accountName,
        @Field("rks") rks: String = this.rks
    )
}

// ------ //

class CertifiedBookmarkServiceImpl(delegate : CertifiedBookmarkService) : CertifiedBookmarkService by delegate {
    override lateinit var accountName: String
    override lateinit var rks: String
}

// ------ //

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
 */
suspend fun CertifiedBookmarkService.postBookmark(
    url: String,
    comment: String = "",
    postTwitter: Boolean = false,
    postFacebook: Boolean = false,
    readLater: Boolean = false,
    private: Boolean = false
) : BookmarkResult = __postBookmark(
    url = url,
    comment = comment,
    postTwitter = postTwitter,
    postFacebook = postFacebook,
    readLater = readLater,
    private = private
)
