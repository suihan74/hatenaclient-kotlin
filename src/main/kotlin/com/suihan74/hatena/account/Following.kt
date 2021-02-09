package com.suihan74.hatena.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Following(
    val name : String
)


@Serializable
data class Follower(
    @SerialName("name")
    val user : String,

    @SerialName("display_name")
    val displayName : String,

    @SerialName("profile_image_url")
    val profileImageUrl : String,

    @SerialName("total_bookmarks")
    val totalBookmarks : Int,

    val private : Boolean,

    @SerialName("followed_by_visitor")
    val followedByVisitor : Boolean
)

// ------ //

@Serializable
data class FollowingsResponse(
    @SerialName("followings")
    val users : List<Following>
)

@Serializable
data class FollowersResponse(
    @SerialName("followers")
    val users : List<Follower>
)