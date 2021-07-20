package com.suihan74.hatena.bookmark

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ブクマに紐づいたtweetのURLとクリック数
 */
@Serializable
data class TweetsAndClicks(
    @SerialName("user_name")
    val user : String,

    @SerialName("tweet_url")
    val tweetUrl : String,

    @SerialName("bookmarked_url")
    val bookmarkedUrl : String,

    val count : Int
)

/**
 * `TweetsAndClicks`を取得するためのリクエスト内容
 */
@Serializable
data class TweetsAndClicksRequestBodyItem(
    val user : String,
    val url : String
)

/**
 * `TweetsAndClicks`を取得するためのリクエスト内容
 */
@Serializable
data class TweetsAndClicksRequestBody(
    val bookmarks : List<TweetsAndClicksRequestBodyItem>
)