package com.suihan74.hatena.star

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Star(
    @SerialName("name")
    val user: String,

    val quote: String,

    val count: Int = 1,

    val color: StarColor = StarColor.YELLOW
) {
    val userIconUrl : String by lazy {
        "http://cdn1.www.st-hatena.com/users/$user/profile.gif"
    }
}