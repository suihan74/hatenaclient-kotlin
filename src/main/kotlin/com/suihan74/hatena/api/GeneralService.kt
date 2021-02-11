package com.suihan74.hatena.api

import com.suihan74.hatena.exception.InvalidResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.suihan74.hatena.exception.HttpException
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GeneralService {
    /**
     * 指定URLにGETリクエストを送る
     */
    @GET
    suspend fun get(@Url url : String) : Response<ResponseBody>
}

/**
 * 対象urlをGETしてレスポンスをhtml文書としてパースする
 *
 * @param url
 * @param parser
 * @throws HttpException 通信失敗
 * @throws InvalidResponseException レスポンスの処理に失敗
 */
suspend fun <T> GeneralService.getHtml(
    url : String,
    parser: (doc: Document)->T
) : T = withContext(Dispatchers.IO) {
    val response = get(url)
    if (response.code() != 200) throw HttpException(response)

    val result = runCatching {
        @Suppress("BlockingMethodInNonBlockingContext")
        response.body()!!.use { body ->
            val html = Jsoup.parse(body.byteStream(), "UTF-8", HatenaClient.baseUrlB)
            parser(html)
        }
    }.onFailure {
        throw InvalidResponseException(cause = it)
    }

    result.getOrNull()!!
}