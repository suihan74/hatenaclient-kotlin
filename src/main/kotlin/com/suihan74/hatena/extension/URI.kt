package com.suihan74.hatena.extension

import java.net.URI

/**
 * URIのクエリパラメータのマップを取得する
 *
 * getterで毎回パースが実行されるので，繰り返し参照する場合は適当にキャッシュすること
 */
val URI.queryParameters : Map<String, String> get() = buildMap {
    query?.split("&")?.forEach { param ->
        val delimiterIndex = param.indexOf("=")
        val key =
            if (delimiterIndex < 0) param
            else param.substring(0, delimiterIndex)
        val value =
            if (delimiterIndex < 0 || delimiterIndex == param.lastIndex) ""
            else param.substring(delimiterIndex + 1)
        put(key, value)
    } ?: emptyMap<String, String>()
}
