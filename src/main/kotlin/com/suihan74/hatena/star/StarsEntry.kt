package com.suihan74.hatena.star

import com.suihan74.hatena.serializer.BooleanAsBinarySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StarsEntry(
    @SerialName("uri")
    val url : String,

    val stars : List<Star>,

    @SerialName("colored_stars")
    val coloredStars : List<ColorStars> = emptyList(),

    @SerialName("can_comment")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val canComment : Boolean = false
) {
    val allStars : List<Star> by lazy {
        val stars = coloredStars.flatMap { it.stars }.plus(stars)
        stars.groupBy { "${it.user},${it.color.name}" }
            .map {
                it.value[0].copy(
                    count = it.value.sumBy { s -> s.count }
                )
            }
    }

    val totalCount : Int by lazy {
        stars.sumBy { it.count } + (coloredStars.sumBy { it.stars.count() })
    }

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
    val canComment : Boolean,

    val rks : String? = null
)