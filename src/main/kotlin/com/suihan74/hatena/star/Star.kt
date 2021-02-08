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
}