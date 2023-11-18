package com.suihan74.hatena.service

import com.suihan74.hatena.api.AccountAPI
import com.suihan74.hatena.api.CertifiedAccountAPI
import com.suihan74.hatena.exception.HatenaException
import com.suihan74.hatena.extension.toUserIconUrl
import com.suihan74.hatena.model.account.*


/**
 * 認証を必要としないアカウントサービス
 */
open class AccountService internal constructor(private val api: AccountAPI) {
    /**
     * 指定ユーザーがフォローしているユーザーリストを取得する
     *
     * @param user 対象ユーザーID
     * @throws HatenaException
     */
    suspend fun getFollowings(user: String) : FollowingsResponse =
        runCatching {
            api.getFollowings(user)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 指定ユーザーをフォローしているユーザーリストを取得する
     *
     * @param user 対象ユーザーID
     * @throws HatenaException
     */
    suspend fun getFollowers(user: String) : FollowersResponse =
        runCatching {
            api.getFollowers(user)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * ユーザーのアイコンURLを取得する
     */
    fun getUserIconUrl(user: String) : String = user.toUserIconUrl

    /**
     * 指定ユーザーが使用したタグを取得する
     *
     * @param user 対象ユーザーID
     * @throws HatenaException
     */
    suspend fun getUserTags(user: String) : List<Tag> =
        runCatching {
            val response = api.getUserTags(user)
            response.tags
                .map {
                    Tag(
                        text = it.key,
                        index = it.value.index,
                        count = it.value.count,
                        timestamp = it.value.timestamp
                    )
                }
                .sortedBy { it.index }
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()
}

// ------ //

/**
 * 認証済みの状態でアクセスできるアカウントサービス
 */
class CertifiedAccountService internal constructor(private val api: CertifiedAccountAPI) : AccountService(api) {
    /**
     * アカウント情報を取得
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getAccount() : Account =
        runCatching {
            api.getAccount()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 通知を取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getNotices() : NoticesResponse =
        runCatching {
            api.getNotices()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    /**
     * 通知最終確認時刻を更新する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun readNotices() : ReadNoticesResponse =
        runCatching {
            api.readNotices()
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * 非表示ユーザーリストを取得
     *
     * @param limit 最大取得件数。`null`, `0`, 負値はすべて`null`として扱われ，適当な件数と追加取得用のカーソルが返される
     * @param cursor 順次取得用カーソル
     * @return 非表示ユーザーリスト(公式設定ページの表示順)とカーソルを含んだレスポンス
     * @throws HatenaException 通信失敗
     */
    suspend fun getIgnoredUsers(
        limit: Int? = null,
        cursor: String? = null
    ) : IgnoredUsersResponse =
        runCatching {
            api.getIgnoredUsers(limit, cursor)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()

    // ------ //

    /**
     * ユーザーを非表示にする
     *
     * @param user 非表示にするユーザーID
     * @throws HatenaException 通信失敗
     */
    suspend fun ignoreUser(user: String) {
        runCatching {
            api.ignoreUser(user)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()
    }

    /**
     * ユーザーを非表示にする
     *
     * @param user 非表示を解除するユーザーID
     * @throws HatenaException code=500: ユーザーが存在しない
     * @throws HatenaException 通信失敗
     */
    suspend fun unIgnoreUser(user: String) {
        runCatching {
            api.unIgnoreUser(user)
        }.onFailure {
            throw HatenaException(cause = it)
        }.getOrThrow()
    }


    /**
     * 非表示ユーザーリストを全件取得
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getIgnoredUsersAll(retryLimit: Int = 3) : IgnoredUsersResponse {
        var cursor: String? = null
        var retryCount = 0
        val users = buildList {
            do {
                runCatching {
                    getIgnoredUsers(limit = null, cursor = cursor)
                }.onSuccess {
                    cursor = it.cursor
                    retryCount = 0
                    addAll(it.users)
                }.onFailure {
                    // 指定回数失敗したら例外送出
                    if (++retryCount >= retryLimit) {
                        throw HatenaException(cause = it)
                    }
                }
            } while (cursor != null)
        }
        return IgnoredUsersResponse(users = users, cursor = cursor)
    }

    /**
     * ログインユーザーが使用しているタグを取得する
     *
     * @throws HatenaException 通信失敗
     */
    suspend fun getUserTags() : List<Tag> {
        return getUserTags(user = api.accountName)
    }
}