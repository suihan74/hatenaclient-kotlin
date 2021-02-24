package com.suihan74.hatena.star

import com.suihan74.hatena.serializer.BooleanAsBinarySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * スター情報
 */
@Serializable
data class StarsEntry(
    /** スターがつけられたURL */
    @SerialName("uri")
    val url : String,

    /** 黄スター */
    val stars : List<Star>,

    /** カラースター */
    @SerialName("colored_stars")
    val coloredStars : List<ColorStars> = emptyList(),

    /** (調査中) */
    @SerialName("can_comment")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val canComment : Boolean = false,

    /** (調査中)canComment==1のとき存在する */
    val comments : List<String> = emptyList()
) {
    /**
     * 全種類のスターをひとまとめにしたリスト
     */
    val allStars : List<Star> by lazy {
        val stars = coloredStars.flatMap { it.stars }.plus(stars)
        stars.groupBy { "${it.user},${it.color.name}" }
            .map {
                it.value[0].copy(
                    count = it.value.sumBy { s -> s.count }
                )
            }
    }

    /**
     * 全スター数
     */
    val totalCount : Int by lazy {
        stars.sumBy { it.count } + (coloredStars.sumBy { it.stars.count() })
    }

    /**
     * 指定色のスター数
     */
    fun starsCount(color: StarColor) : Int = when (color) {
        StarColor.YELLOW -> stars.sumBy { it.count }
        else -> coloredStars.first { it.color == color }.starsCount
    }
}

// ------ //

/**
 * 複数URLに対する一括のスターエントリ取得レスポンス
 */
@Serializable
data class StarsEntriesResponse(
    val entries : List<StarsEntry>,

    @SerialName("can_comment")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val canComment : Boolean = false,

    /** (調査中) */
    val comments : List<String> = emptyList(),

    /** 要サインインなスターAPIを使用する際に必要なrks */
    val rks : String? = null
)