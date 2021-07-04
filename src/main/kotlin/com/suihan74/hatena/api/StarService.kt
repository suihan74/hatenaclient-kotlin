package com.suihan74.hatena.api

import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.star.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import retrofit2.http.*
import java.time.LocalDateTime
import java.time.ZoneOffset

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
    val rks : String
    val rk : String

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
     * 所有しているカラースター数を取得する
     */
    @GET("api/v0/me/colorstars")
    suspend fun getMyColorStars(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("X-Internal-API-Key") apiKey: String = "wvlYJXSGDMY161Bbw4TEf8unWl4pDLLB1gy7PGcA",
        @Header("X-Internal-API-RK") rk: String = this.rk
    ) : UserColorStarCountsResponse

    /**
     * 対象URL上での現在のカラーパレット状態を取得する
     */
    @GET("colorpalette.json")
    suspend fun getColorPalette(
        @Query("uri") url: String,
        @Query("date") date: Long
    ) : StarPalette

    /**
     * 対象URLでのカラーパレット状態を変更する
     */
    @POST("colorpalette.json")
    suspend fun changeColorPalette(
        @Query("uri") url: String,
        @Query("color") color: StarColor,
        @Query("token") palette: StarPalette
    ) : Response<*>

    /**
     * スターを投稿する
     */
    @GET("star.add.json")
    suspend fun __postStar(
        @Query("uri") url: String,
        @Query("quote") quote: String? = null,
        @Query("rks") rks: String = this.rks
    )

    /**
     * スターを削除する
     */
    @GET("star.delete.json")
    suspend fun deleteStar(
        @Query("uri") url: String,
        @Query("name") userName: String,
        @Query("color") color: StarColor,
        @Query("quote") quote: String,
        @Query("rks") rks: String = this.rks
    )
}

/**
 * スターを投稿する
 */
suspend fun CertifiedStarService.postStar(
    url: String,
    color: StarColor,
    quote: String? = null
) {
    val palette = getColorPalette(url, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
    if (color == palette.color) {
        return
    }
    else if (!palette.colorStarCounts.has(color)) {
        return // throw exception
    }

    val response = changeColorPalette(url, color, palette)

    if (response.isSuccessful) {
        __postStar(url, quote)
    }
}

// ------ //

class CertifiedStarServiceImpl(
    delegate : CertifiedStarService
) : CertifiedStarService by delegate {
    override lateinit var accountName : String
    override lateinit var rks : String
    override lateinit var rk : String
}