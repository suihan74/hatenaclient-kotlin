package com.suihan74.hatena.service

import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.api.GeneralAPI
import com.suihan74.hatena.exception.HatenaException
import com.suihan74.hatena.exception.InvalidResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import retrofit2.Response
import java.nio.charset.Charset

class GeneralService internal constructor(private val api: GeneralAPI) {
    /**
     * 指定URLにGETリクエストを送る
     */
    suspend fun get(url : String) : Response<ResponseBody> =
        runCatching {
            api.get(url)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 対象urlをGETしてレスポンスをhtml文書としてパースする
     *
     * @param url
     * @param parser
     * @throws HttpException 通信失敗
     * @throws InvalidResponseException レスポンスの処理に失敗
     */
    suspend fun <T> getHtml(
        url: String,
        parser: suspend (doc: Document)->T
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

// ------ //

    suspend fun <T> getHtmlDetectedCharset(
        url: String,
        parser: suspend (doc: Document)->T
    ) : T = withContext(Dispatchers.IO) {
        val response = get(url)
        if (!response.isSuccessful) throw IllegalStateException("")

        val bodyBytes = response.body().use { it!!.bytes() }

        // 文字コードを判別してから内容を読み込む
        val defaultCharsetName = Charset.defaultCharset().name().lowercase()
        var charsetName = defaultCharsetName
        var charsetDetected = false

        val charsetRegex = Regex("""charset=([a-zA-Z\d_\-]+)""")
        fun parseCharset(src: String) : String {
            val m = charsetRegex.find(src)
            return if ((m?.groups?.size ?: 0) >= 2) m!!.groups[1]!!.value.lowercase() else ""
        }

        // レスポンスヘッダで判断できる場合
        val contentType = response.headers().firstOrNull { it.first == "Content-Type" }?.second
        if (contentType?.isNotEmpty() == true) {
            val parsed = parseCharset(contentType)
            if (parsed.isNotEmpty()) {
                charsetName = parsed
                charsetDetected = true
            }
        }

        val rawHtml = bodyBytes.toString(Charset.defaultCharset())
        val rawDoc = Jsoup.parse(rawHtml)

        if (!charsetDetected) {
            rawDoc.getElementsByTag("meta").let { elem ->
                // <meta charset="???">
                val charsetMeta = elem.firstOrNull { it.hasAttr("charset") }
                if (charsetMeta != null) {
                    charsetName = charsetMeta.attr("charset").lowercase()
                    charsetDetected = true
                    return@let
                }
                // <meta http-equiv="Content-Type" content="text/html; charset=???">
                val meta = elem.firstOrNull { it.attr("http-equiv").lowercase() == "content-type" }
                meta?.attr("content")?.let {
                    val parsed = parseCharset(it)
                    if (parsed.isNotEmpty()) {
                        charsetName = parsed
                        charsetDetected = true
                    }
                }
            }
        }
        // shift-jisは実体はms932であると決めつける
        when (charsetName) {
            "shift-jis", "shift_jis", "sjis" -> charsetName = "MS932"
        }

        val result = runCatching {
            @Suppress("BlockingMethodInNonBlockingContext")
            response.body()!!.use {
                val doc =
                    if (charsetName == defaultCharsetName) rawDoc
                    else Jsoup.parse(bodyBytes.inputStream(), charsetName, url)
                parser(doc)
            }
        }.onFailure {
            throw InvalidResponseException(cause = it)
        }

        result.getOrNull()!!
    }

}
