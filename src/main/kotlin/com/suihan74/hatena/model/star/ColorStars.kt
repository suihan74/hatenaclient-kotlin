package com.suihan74.hatena.model.star

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * スターリストのうち，カラースターを表す情報
 */
@Serializable
data class ColorStars(
    val color : StarColor,

    @SerialName("stars")
    private val _stars : List<Star>? = null
    // color=yellowであるので注意
    // `MyRecentStars()`のレスポンスの場合，
    // カラースターが1件以上あると [{"color":"green","stars":null}] と取得される
) {
    /** カラー情報を反映したスターリスト */
    val stars : List<Star> by lazy {
        _stars?.map { it.copy(color = this.color) }
            ?: listOf(Star(color = color, count = 1, quote = "", user = ""))
    }

    /** 総スター数 */
    val starsCount : Int by lazy {
        stars.sumOf { it.count }
    }
}
