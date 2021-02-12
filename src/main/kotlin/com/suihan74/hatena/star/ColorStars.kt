package com.suihan74.hatena.star

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * スターリストのうち，カラースターを表す情報
 */
@Serializable
data class ColorStars(
    val color : StarColor,

    @SerialName("stars")
    private val _stars : List<Star>  // color=yellowであるので注意
) {
    /** カラー情報を反映したスターリスト */
    val stars : List<Star> by lazy {
        _stars.map { it.copy(color = this.color) }
    }

    /** 総スター数 */
    val starsCount : Int by lazy {
        stars.sumBy { it.count }
    }
}
