package com.suihan74.hatena.exception

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

typealias HttpException = retrofit2.HttpException

/**
 * `HttpException`をHatenaClient内から送出する
 */
internal fun createHttpException(code: Int, message: String) : HttpException {
    val dummyResponse = okhttp3.Response.Builder()
        .body(ByteArray(0).toResponseBody(null))
        .code(code)
        .message(message)
        .protocol(Protocol.HTTP_1_1)
        .request(Request.Builder().url("http://localhost/").build())
        .build()
    return HttpException(Response.error<String>("".toResponseBody(null), dummyResponse))
}