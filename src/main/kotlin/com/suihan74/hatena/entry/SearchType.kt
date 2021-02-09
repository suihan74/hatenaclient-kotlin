package com.suihan74.hatena.entry

enum class SearchType(val code : String) {
    TAG("tag"),
    TEXT("text"),
    ;

    companion object {
        fun fromOrdinal(idx: Int) = values().getOrElse(idx) { TAG }
    }
}