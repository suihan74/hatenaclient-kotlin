package com.suihan74.hatena.api

import com.suihan74.hatena.entry.Category
import com.suihan74.hatena.entry.EntriesType
import com.suihan74.hatena.entry.Entry
import com.suihan74.hatena.extension.int
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * エントリ関連のAPI
 */
interface EntryService {
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
    ) : List<Entry>

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
    suspend fun getEntries(
        entriesType: EntriesType,
        category: Category,
        limit: Int? = null,
        offset: Int? = null,
        includeAMPUrls: Boolean = true,
        includeBookmarkedData: Boolean = true,
        includeBookmarksOfFollowings: Boolean = true,
        includeAds: Boolean = false
    ) : List<Entry>
}

// ------ //

class EntryServiceImpl(delegate : EntryService) : EntryService by delegate {
    /**
     * ホットエントリを取得する
     *
     * @see EntryService.getEntries
     */
    override suspend fun getEntries(
        entriesType: EntriesType,
        category: Category,
        limit: Int?,
        offset: Int?,
        includeAMPUrls: Boolean,
        includeBookmarkedData: Boolean,
        includeBookmarksOfFollowings: Boolean,
        includeAds: Boolean
    ) : List<Entry> = __getEntries(
        entriesType.code,
        category.code,
        limit, offset,
        includeAMPUrls.int, includeBookmarkedData.int, includeBookmarksOfFollowings.int, includeAds.int
    )
}