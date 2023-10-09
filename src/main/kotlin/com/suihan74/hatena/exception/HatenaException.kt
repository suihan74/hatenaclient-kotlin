package com.suihan74.hatena.exception

/**
 * HatenaClientから送出する例外ベースクラス
 */
open class HatenaException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable(message, cause)
