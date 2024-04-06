package com.suihan74.hatena.api

import com.suihan74.hatena.model.bookmark.*
import com.suihan74.hatena.service.GeneralService
import retrofit2.http.*

/**
 * ブックマーク関係のAPI
 */
interface BookmarkAPI {
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
     * @see BookmarkAPI.getBookmarksCount
     */
    @GET("https://bookmark.hatenaapis.com/count/entries")
    suspend fun getBookmarksCount(
        @Query("url") urls: List<String>
    ) : Map<String, Int>

    /**
     * 指定URLに対するユーザーのブクマに紐づいたツイートとそのクリック数を取得する
     */
    @POST("api/internal/bookmarks/tweets_and_clicks")
    suspend fun getTweetsAndClicks(
        @Body requestBody: TweetsAndClicksRequestBody
    ) : List<TweetsAndClicks>
}

// ------ //

/**
 * 認証が必要なブックマーク関係API
 */
interface CertifiedBookmarkAPI : BookmarkAPI {
    val accountName : String

    val rks : String

    val generalService : GeneralService

    /**
     * ブックマークを投稿する
     *
     * 2021年時点でEvernoteは使用不可能
     *
     * @see CertifiedBookmarkAPI.postBookmark
     */
    @FormUrlEncoded
    @POST("{account}/add.edit.json")
    suspend fun postBookmark(
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

    @POST("-/report/bookmark")
    suspend fun report(
        @Field("url") url: String,
        @Field("user_name") userName: String,
        @Field("category") category: String,
        @Field("text") text: String,
        @Field("rks") rks: String = this.rks
    )
}

// ------ //

class CertifiedBookmarkAPIImpl(delegate : CertifiedBookmarkAPI) : CertifiedBookmarkAPI by delegate {
    override lateinit var accountName: String
    override lateinit var rks: String
    override lateinit var generalService: GeneralService
}
