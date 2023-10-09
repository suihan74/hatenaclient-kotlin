package com.suihan74.hatena.model.account

import kotlinx.serialization.Serializable

/**
 * 非表示ユーザーリスト取得時のレスポンス
 */
@Serializable
data class IgnoredUsersResponse (
    /** 非表示ユーザーのIDリスト */
    val users : List<String>,

    /** 順次取得用のカーソル (nullの場合最後までロード完了) */
    val cursor : String? = null
)
