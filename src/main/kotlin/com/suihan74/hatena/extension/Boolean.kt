package com.suihan74.hatena.extension

/**
 * 真偽値を整数値に変換する
 */
val Boolean.int
    get() = if (this) 1 else 0
