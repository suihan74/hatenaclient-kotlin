package com.suihan74.hatena.api

import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.star.StarColor
import com.suihan74.hatena.star.StarPalette
import com.suihan74.hatena.star.StarsEntriesResponse
import com.suihan74.hatena.star.StarsEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * スター関連のAPI
 */
interface StarService {
    /**
     * 指定URLにつけられたスターを取得する
     */
    @GET("entry.json")
    suspend fun __getStarsEntry(@Query("uri") url: String) : StarsEntriesResponse

    /**
     * 複数渡されたURLにつけられたスターを取得する
     */
    @GET("entry.json")
    suspend fun __getStarsEntries(@Query("uri") urls: List<String>) : StarsEntriesResponse
}

/**
 * 複数渡されたURLにつけられたスターを取得する
 */
suspend fun StarService.getStarsEntry(url: String) : StarsEntry {
    val response = __getStarsEntry(url)
    return response.entries.firstOrNull() ?: StarsEntry(url = url, stars = emptyList())
}

/**
 * 複数渡されたURLにつけられたスターを取得する
 */
suspend fun StarService.getStarsEntries(urls: List<String>) : List<StarsEntry> {
    val windowed = urls.windowed(50, 50, partialWindows = true)
    val tasks = windowed.map {
        coroutineScope { async {
            __getStarsEntries(it)
        } }
    }
    val responses = tasks.awaitAll()
    return responses.flatMap { it.entries }
}

// ------ //

/**
 * 認証が必要なスター関連のAPI
 */
interface CertifiedStarService : StarService {
    val accountName : String

    /**
     * スターAPI用のrks取得
     */
    @GET("entries.json")
    suspend fun __getCredential() : StarsEntriesResponse

    /**
     * 最近自分がつけたスターを取得する
     *
     * @throws HttpException 403: 自分以外のユーザー名が渡された場合
     */
    @GET("{userId}/stars.json")
    suspend fun getMyRecentStars(
        @Path("userId") userId: String = this.accountName
    ) : List<StarsEntry>

    /**
     * 最近自分に対してつけられたスターを取得する
     *
     * @throws HttpException 403: 自分以外のユーザー名が渡された場合
     */
    @GET("{userId}/report.json")
    suspend fun getRecentStarsReport(
        @Path("userId") userId: String = this.accountName
    ) : StarsEntriesResponse

    // ------ //

    /**
     * 対象URL上での現在のカラーパレット状態を取得する
     */
    @GET("colorpalette.json")
    suspend fun getColorPalette(
        @Query("uri") url: String,
        @Query("date") date: Int
    ) : StarPalette

    /**
     * 対象URLでのカラーパレット状態を変更する
     */
    @POST("colorpalette.json")
    suspend fun changeColorPalette(
        @Query("uri") url: String,
        @Query("color") color: StarColor,
        @Query("token") palette: StarPalette
    )
}

// ------ //

class CertifiedStarServiceImpl(
    delegate : CertifiedStarService
) : CertifiedStarService by delegate {
    override lateinit var accountName : String
}