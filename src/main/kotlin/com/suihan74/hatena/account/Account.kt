package com.suihan74.hatena.account

import com.suihan74.hatena.account.serializer.BooleanAsBinarySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * アカウント情報
 */
@Serializable
data class Account(
    @Serializable(with = BooleanAsBinarySerializer::class)
    val login : Boolean,

    val name : String,

    val sex : String,

    val rks : String,

    @SerialName("plususer")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val plusUser : Boolean,

    @SerialName("no_ads")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val noAds : Boolean,

    @SerialName("favorite_count")
    val favoriteCount : Long,

    @SerialName("interest_words_has_been_used")
    val interestWordsHasBeenUsed : Long,

    @SerialName("ignores_regex")
    val ignoresRegex : String,

    @SerialName("is_oauth_twitter")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val isOAuthTwitter : Boolean,

    @SerialName("is_oauth_facebook")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val isOAuthFaceBook : Boolean,

    @SerialName("is_oauth_evernote")
    @Serializable(with = BooleanAsBinarySerializer::class)
    val isOAuthEvernote : Boolean,

    @SerialName("twitter_checked")
    val twitterChecked : String,
)
