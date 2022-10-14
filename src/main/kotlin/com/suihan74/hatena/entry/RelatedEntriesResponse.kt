package com.suihan74.hatena.entry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RelatedEntriesResponse(
    val entries : List<EntryItem>,

    @SerialName("meta_entry")
    val metaEntry : EntryItem?,

    @SerialName("refered_blog_entries")
    val referredBlogEntries : List<EntryItem>,

    @SerialName("refered_entries")
    val referredEntries : List<EntryItem>,

    val topics : List<String>,

    @SerialName("pr_entries")
    val prEntries: List<EntryItem>,
)