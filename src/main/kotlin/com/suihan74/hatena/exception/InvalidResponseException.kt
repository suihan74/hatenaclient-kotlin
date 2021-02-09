package com.suihan74.hatena.exception

/**
 * レスポンスが不正
 */
class InvalidResponseException(msg: String? = null, cause: Throwable? = null) : Throwable(msg, cause)