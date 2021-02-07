package com.suihan74.hatena.star

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Star(
    @SerialName("name")
    val user: String,

    val quote: String,

    val color: StarColor,

    val count: Int
) {
    val userIconUrl : String by lazy {
        "http://cdn1.www.st-hatena.com/users/$user/profile.gif"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Star) return false
        return color == other.color && count == other.count && quote == other.quote
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + quote.hashCode()
        result = 31 * result + count
        result = 31 * result + (color.hashCode())
        return result
    }
}