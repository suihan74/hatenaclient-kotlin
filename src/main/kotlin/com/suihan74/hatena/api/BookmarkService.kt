package com.suihan74.hatena.api

import com.suihan74.hatena.bookmark.BookmarksDigest
import com.suihan74.hatena.bookmark.BookmarksEntry
import com.suihan74.hatena.bookmark.BookmarksResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    // ------ //

    @GET("https://bookmark.hatenaapis.com/count/entries")
    suspend fun __getBookmarksCount(
        @Query("url") urls: List<String>
    ) : Map<String, Int>
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