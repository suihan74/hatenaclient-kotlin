package com.suihan74.hatena.model.entry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 特集
 */
@Serializable
data class Issue (
    val name: String,

    @SerialName("issue_id")
    val code: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    val entry: IssueEntry? = null,
)

// ------ //

@Serializable
data class IssuesResponse(val issues: List<Issue>)

@Serializable
data class IssueEntriesResponse(val issue: Issue, val entries: List<IssueEntry>)