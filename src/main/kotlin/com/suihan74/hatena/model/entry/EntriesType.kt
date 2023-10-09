package com.suihan74.hatena.model.entry

enum class EntriesType(
    val code : String,
    val codeForIssues : String,
    val codeForSearch : String
) {
    HOT("hotentry", "hotentries", "hot"),

    RECENT("newentry", "newentries", "recent"),
    ;

    companion object {
        fun fromOrdinal(idx: Int) = values().getOrElse(idx) { HOT }
    }
}

// ------ //

internal enum class EntriesTypeUsage {
    ENTRIES,
    ISSUE_ENTRIES,
    SEARCH_SORT
}

/**
 * EntriesTypeの扱い方がAPIによって異なるので，
 * どの用途で使用されているかによって使用するcodeをスイッチさせる
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class EntriesTypeQuery(
    val value : EntriesTypeUsage
)
