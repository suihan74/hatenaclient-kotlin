package com.suihan74.hatena.extension

/**
 * ユーザー名の文字列からアイコンURLを取得する
 */
internal val String.toUserIconUrl : String get() =
    "https://cdn1.www.st-hatena.com/users/$this/profile.gif"

//https://cdn.profile-image.st-hatena.com/users/$this/profile.png