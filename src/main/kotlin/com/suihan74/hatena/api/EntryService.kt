package com.suihan74.hatena.api

import com.suihan74.hatena.entry.*
import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.exception.InvalidResponseException
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.URLEncoder

/**
 * エントリ関連のAPI
 */
interface EntryService {
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
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true
    ) : List<EntryItem>
}

/**
 * 指定ページのエントリIDを取得する
 *
 * @throws HttpException 通信失敗
 * @throws InvalidResponseException レスポンスの処理に失敗
 */
suspend fun EntryService.getEntryId(url: String) : Long {
    val entryUrl = HatenaClient.getEntryUrl(url)
    return HatenaClient.generalService.getHtml(entryUrl) { html ->
        html.getElementsByTag("html")!!
            .first()
            .attr("data-entry-eid")
            .toLong()
    }
}

/**
 * エントリIDから対象のページのURLを取得する
 *
 * @param eid エントリID
 * @return 対象ページのURL
 * @throws HttpException 通信失敗
 */
suspend fun EntryService.getUrl(eid: Long) : String {
    val baseUrl = HatenaClient.baseUrlB
    val eidEntryUrl = buildString { append(baseUrl, "entry/", eid) }
    return HatenaClient.generalService.get(eidEntryUrl).let { response ->
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
}

/**
 * 与えられたページのfaviconのURLを取得する
 *
 * @return faviconのURL(実際にはてながキャッシュしていて画像が取得できるかは考慮しない)
 */
fun EntryService.getFaviconUrl(url: String) : String =
    "https://cdn-ak2.favicon.st-hatena.com/?url=${URLEncoder.encode(url, "UTF-8")}"

// ------ //

/**
 * 認証が必要なエントリ関係API
 */
interface CertifiedEntryService : EntryService {
    /**
     * ユーザーがブクマしたエントリ一覧を取得する
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
}
