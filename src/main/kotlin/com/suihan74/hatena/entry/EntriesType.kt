package com.suihan74.hatena.entry

enum class EntriesType(val code : String) {
    HOT("hotentry"),

    RECENT("newentry"),
    ;

    companion object {
        fun fromOrdinal(idx: Int) = values().getOrElse(idx) { HOT }
    }
}