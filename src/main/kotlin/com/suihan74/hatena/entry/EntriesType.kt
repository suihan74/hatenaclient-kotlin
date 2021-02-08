package com.suihan74.hatena.entry

enum class EntriesType(
    val code : String,
    val codeForIssues : String,
) {
    HOT("hotentry", "hotentries"),

    RECENT("newentry", "newentries"),
    ;

    companion object {
        fun fromOrdinal(idx: Int) = values().getOrElse(idx) { HOT }
    }
}