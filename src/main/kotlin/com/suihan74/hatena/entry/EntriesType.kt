package com.suihan74.hatena.entry

enum class EntriesType(val code : String) {
    Hot("hotentry"),
    Recent("newentry"),
    ;

    companion object {
        fun fromOrdinal(idx: Int) = values().getOrElse(idx) { Hot }
    }
}