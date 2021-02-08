package com.suihan74.hatena.api

import com.suihan74.hatena.entry.*
import com.suihan74.hatena.extension.int
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * エントリ関連のAPI
 */
interface EntryService {
    /**
     * 人気/新着エントリを取得する
     *
     * @see EntryService.getEntries
     */
    @GET("/api/ipad.{type}.json")
    suspend fun __getEntries(
        @Path("type") entriesType: String,
        @Query("category_id") categoryCode: String,
        @Query("limit") limit: Int?,
        @Query("of") offset: Int?,
        @Query("include_amp_urls") includeAMPUrls: Int,
        @Query("include_bookmarked_data") includeBookmarkedData: Int,
        @Query("include_bookmarks_of_followings") includeBookmarksOfFollowings: Int,
        @Query("ad") includeAds: Int
    ) : List<EntryItem>

    /**
     * 指定カテゴリの特集一覧を取得する
     */
    @GET("api/internal/cambridge/category/{category_id}/issues")
    suspend fun __getIssues(@Path("category_id") categoryCode: String) : IssuesResponse
}

// ------ //

/**
 * 人気/新着エントリを取得する
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
suspend fun EntryService.getEntries(
    entriesType: EntriesType,
    category: Category,
    limit: Int? = null,
    offset: Int? = null,
    includeAMPUrls: Boolean = true,
    includeBookmarkedData: Boolean = true,
    includeBookmarksOfFollowings: Boolean = true,
    includeAds: Boolean = false
) : List<EntryItem> = __getEntries(
    entriesType.code,
    category.code,
    limit, offset,
    includeAMPUrls.int, includeBookmarkedData.int, includeBookmarksOfFollowings.int, includeAds.int
)

/**
 * 指定カテゴリの特集一覧を取得する
 *
 * @param category カテゴリ
 */
suspend fun EntryService.getIssues(category: Category) : List<Issue> =
    __getIssues(category.code).issues

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
     */
    @GET("api/ipad.mybookmarks")
    suspend fun getBookmarkedEntries(
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null
    ) : List<EntryItem>
}
