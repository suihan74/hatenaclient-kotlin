package com.suihan74.hatena.api

import com.suihan74.hatena.bookmark.BookmarksDigest
import com.suihan74.hatena.bookmark.BookmarksEntry
import com.suihan74.hatena.bookmark.BookmarksResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * ブクマ関係のAPI
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
}
