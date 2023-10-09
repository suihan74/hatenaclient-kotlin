package com.suihan74.hatena.model.bookmark

import kotlinx.serialization.Serializable

/** 追加ロードのためのカーソルを含んだブコメリスト取得用のレスポンス */
@Serializable
data class BookmarksResponse (
    val cursor: String?,
    val bookmarks: List<Bookmark>
)
