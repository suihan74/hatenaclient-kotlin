package com.suihan74.hatena.entry

/**
 * エントリカテゴリ
 */
enum class Category(
    val id: Int,
    val code: String
) {
    ALL(0, "315767106563433873"),
    GENERAL(1, "315756341902288872"),
    SOCIAL(2, "301816409282464093"),
    ECONOMICS(3, "300989576564947867"),
    LIFE(4, "244148959988020477"),
    KNOWLEDGE(5, "315890158150969179"),
    IT(6, "261248828312298389"),
    ENTERTAINMENT(7, "302115476501939948"),
    GAME(8, "297347994088281699"),
    FUN(9, "302115476506048236"),
//    CURRENT_EVENTS(10, "83497569613451046"),
    ;

    companion object {
        fun fromId(id: Int) = values().firstOrNull { it.id == id } ?: ALL
    }
}