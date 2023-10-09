package com.suihan74.hatena.api

import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.model.entry.*
import com.suihan74.hatena.service.GeneralService
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant

/**
 * エントリ関連のAPI
 */
interface EntryAPI {
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
     */
    @GET("api/ipad.{type}.json")
    suspend fun getEntries(
        @Path("type") @EntriesTypeQuery(EntriesTypeUsage.ENTRIES) entriesType: EntriesType,
        @Query("category_id") category: Category,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_amp_urls") includeAMPUrls: Boolean = true,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true,
        @Query("include_bookmarks_of_followings") includeBookmarksOfFollowings: Boolean = true,
        @Query("ad") includeAds: Boolean = false
    ) : List<EntryItem>

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
     */
    @GET("api/internal/cambridge/issue/{issue_id}/{type}")
    suspend fun getIssueEntries(
        @Path("type") @EntriesTypeQuery(EntriesTypeUsage.ISSUE_ENTRIES) entriesType: EntriesType,
        @Path("issue_id") issue: Issue,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_amp_urls") includeAMPUrls: Boolean = true,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true,
        @Query("include_bookmarks_by_visitor") includeBookmarksByVisitor: Boolean = true,
        @Query("include_bookmarks_of_followings") includeBookmarksOfFollowings: Boolean = true,
        @Query("ad") includeAds: Boolean = false
    ) : IssueEntriesResponse

    /**
     * 指定カテゴリの特集一覧を取得する
     */
    @GET("api/internal/cambridge/category/{category_id}/issues")
    suspend fun getIssues(@Path("category_id") category: Category) : IssuesResponse

    // ------ //

    /**
     * エントリをクエリ検索する
     */
    @GET("api/ipad.search/{searchType}")
    suspend fun searchEntries(
        @Path("searchType") searchType: SearchType,
        @Query("q") query: String,
        @Query("sort") @EntriesTypeQuery(EntriesTypeUsage.SEARCH_SORT) sortType: EntriesType = EntriesType.RECENT,
        @Query("users") users: Int? = null,
        @Query("date_begin") dateBegin: Instant? = null,
        @Query("date_end") dateEnd: Instant? = null,
        @Query("safe") safe: Boolean = false,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true
    ) : List<EntryItem>

    // ------ //

    /**
     * 指定ユーザーがブクマしたエントリを取得する
     *
     * @see EntryAPI.getBookmarkedEntries
     */
    @GET("api/internal/user/{user}/bookmarks")
    suspend fun __getBookmarkedEntries(
        @Path("user") user: String,
        @Query("tag") tag: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null
    ) : UserEntryResponse

    // ------ //

    /**
     * 指定URLのブクマ数を取得する（同時最大50件）
     *
     * ユーザーが利用するために同一のAPIを`BookmarkService`にも用意しているが、
     * 当クラスのものは`EntryService#getHistoricalEntries`で使用するためのもの
     */
    @GET("https://bookmark.hatenaapis.com/count/entries")
    suspend fun __getBookmarksCount(
        @Query("url") urls: List<String>
    ) : Map<String, Int>

    /**
     * 15周年ページのはてな全体の過去人気エントリリストを取得する
     *
     * ブクマ数は別途取得する必要がある
     */
    @GET("15th/entries/{year}.json")
    suspend fun __getHistoricalEntries(
        @Path("year") year: Int
    ) : HatenaHistoricalEntry

    /**
     * 関連エントリを取得する
     */
    @GET("api/ipad.related_entry.json")
    suspend fun getRelatedEntries(
        @Query("url") url: String,
        @Query("ad") ad: Boolean = false
    ) : RelatedEntriesResponse
}

// ------ //

/**
 * 認証が必要なエントリ関係API
 */
interface CertifiedEntryAPI : EntryAPI {
    val accountName : String

    val generalService : GeneralService

    /**
     * サインインユーザーがブクマしたエントリ一覧を取得する
     *
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @throws HttpException 通信失敗
     */
    @GET("api/ipad.mybookmarks")
    suspend fun getBookmarkedEntries(
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null
    ) : List<EntryItem>

    /**
     * サインインユーザーがブクマしたエントリを検索する
     */
    @GET("api/ipad.mysearch/{search_type}")
    suspend fun searchBookmarkedEntries(
        @Path("search_type") searchType: SearchType,
        @Query("q") query: String,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null
    ) : List<EntryItem>

    /**
     * マイホットエントリを取得する
     */
    @GET("api/entries/myhotentry.json")
    suspend fun getMyHotEntries(
        @Query("date") date: String? = null,
        @Query("include_amp_urls") includeAMPUrls: Boolean = true
    ) : List<MyHotEntry>

    @GET("api/my/15th/yearly_random_bookmarks")
    suspend fun __getUserHistoricalEntries(
        @Query("year") year: Int,
        @Query("limit") limit: Int = 10
    ) : List<UserHistoricalEntry>

    /**
     * フォロー中ユーザーがブクマしたエントリを取得する
     */
    @GET("api/internal/cambridge/user/my/feed/following/bookmarks")
    suspend fun __getFollowingEntries(
        @Query("include_amp_urls") includeAmpUrls: Boolean = true,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ) : FollowingEntriesResponse
}

class CertifiedEntryAPIImpl(delegate : CertifiedEntryAPI) : CertifiedEntryAPI by delegate {
    override lateinit var accountName: String
    override lateinit var generalService: GeneralService
}
