package com.suihan74.hatena.star

import com.suihan74.hatena.serializer.StarColorSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StarPalette(
    val token : String,

    @SerialName("entry_uri")
    val entryUrl : String,

    @Serializable(with = StarColorSerializer::class)
    val color : StarColor,

    @SerialName("color_star_counts")
    val colorStarCounts : UserColorStarCounts
)