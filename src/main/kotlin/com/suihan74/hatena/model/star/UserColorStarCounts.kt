package com.suihan74.hatena.model.star

import kotlinx.serialization.Serializable

@Serializable
data class UserColorStarCounts(
    val red : Int,
    val green : Int,
    val blue : Int,
    val purple : Int
) {
    fun has(color: StarColor) : Boolean =
        0 < when (color) {
            StarColor.YELLOW -> 1
            StarColor.RED -> red
            StarColor.GREEN -> green
            StarColor.BLUE -> blue
            StarColor.PURPLE -> purple
        }
}

// ------ //

@Serializable
data class UserColorStarCountsResponse(
    val success : Boolean,
    val result : Map<String, UserColorStarCounts>,
    val message : String? = ""
) {
    val stars : UserColorStarCounts?
        get() = result["counts"]
}
