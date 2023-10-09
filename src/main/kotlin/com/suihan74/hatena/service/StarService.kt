package com.suihan74.hatena.service

import com.suihan74.hatena.api.CertifiedStarAPI
import com.suihan74.hatena.api.StarAPI
import com.suihan74.hatena.exception.HatenaException
import com.suihan74.hatena.model.star.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import java.time.ZoneOffset

open class StarService internal constructor(private val api: StarAPI) {
    /**
     * 複数渡されたURLにつけられたスターを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getStarsEntry(url: String) : StarsEntry =
        runCatching {
            val response = api.__getStarsEntry(url)
            response.entries.firstOrNull() ?: StarsEntry(url = url, stars = emptyList())
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 複数渡されたURLにつけられたスターを取得する
     *
     * @throws HatenaException ひとつでも通信失敗
     */
    suspend fun getStarsEntries(urls: List<String>) : List<StarsEntry> =
        runCatching {
            val windowed = urls.windowed(50, 50, partialWindows = true)
            val tasks = windowed.map { w ->
                coroutineScope {
                    async { api.__getStarsEntries(w) }
                }
            }
            val responses = tasks.awaitAll()
            responses.flatMap { it.entries }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()
}

// ------ //

class CertifiedStarService internal constructor(private val api: CertifiedStarAPI) : StarService(api) {
    /**
     * スターAPI用のrks取得
     *
     * @throws HatenaException 通信失敗
     */
    internal suspend fun getCredential() : StarsEntriesResponse =
        runCatching {
            api.__getCredential()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 最近自分がつけたスターを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getMyRecentStars() : List<StarsEntry> =
        runCatching {
            api.getMyRecentStars()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 最近自分に対してつけられたスターを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getRecentStarsReport() : StarsEntriesResponse =
        runCatching {
            api.getRecentStarsReport()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 所有しているカラースター数を取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getMyColorStars() : UserColorStarCountsResponse =
        runCatching {
            api.getMyColorStars()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 対象URL上での現在のカラーパレット状態を取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getColorPalette(
        url: String,
        date: Long
    ) : StarPalette =
        runCatching {
            api.getColorPalette(url, date)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 対象URLでのカラーパレット状態を変更する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun changeColorPalette(
        url: String,
        color: StarColor,
        palette: StarPalette
    ) =
        runCatching {
            api.changeColorPalette(url, color, palette)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * スターを削除する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun deleteStar(
        url: String,
        userName: String,
        color: StarColor,
        quote: String
    ) =
        runCatching {
            api.deleteStar(url, userName, color, quote)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * スターを投稿する
     */
    suspend fun postStar(
        url: String,
        color: StarColor,
        quote: String? = null
    ) =
        runCatching {
            val palette = getColorPalette(url, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
            if (color == palette.color) {
                return@runCatching
            }
            else if (!palette.colorStarCounts.has(color)) {
                throw HatenaException(message = "do not have ${color.name} stars")
            }

            val response = changeColorPalette(url, color, palette)
            if (response.isSuccessful) {
                api.__postStar(url, quote)
            }
            else {
                throw HatenaException()
            }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()
}