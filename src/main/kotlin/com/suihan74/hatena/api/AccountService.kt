package com.suihan74.hatena.api

import com.suihan74.hatena.account.Account
import com.suihan74.hatena.account.IgnoredUsersResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * アカウント関係のAPI
 */
interface AccountService {
    /**
     * Basic認証でサインインする
     *
     * @param name ユーザー名
     * @param password パスワード
     * @return 成功時: アカウント情報
     */
    @FormUrlEncoded
    @POST("${HatenaClientBase.baseUrlW}login")
    suspend fun __signInImpl(
        @Field("name") name: String,
        @Field("password") password: String
    ) : Response<ResponseBody>
}

// ------ //

/**
 * 要認証のアカウント関係API
 */
interface VerifiedAccountService : AccountService {
    /**
     * アカウント情報を取得
     */
    @GET("my.name")
    suspend fun getAccount() : Account

    // ------ //

    /**
     * 非表示ユーザーリストを取得
     *
     * @param limit 最大取得件数。`null`, `0`, 負値はすべて`null`として扱われ，適当な件数と追加取得用のカーソルが返される
     * @param cursor 順次取得用カーソル
     * @return 非表示ユーザーリスト(公式設定ページの表示順)とカーソルを含んだレスポンス
     */
    @GET("api/my/ignore_users")
    suspend fun getIgnoredUsers(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ) : IgnoredUsersResponse

    /**
     * 非表示ユーザーリストを全件取得
     */
    suspend fun getIgnoredUsersAll() : IgnoredUsersResponse

    /**
     * ユーザーを非表示にする
     *
     * @param user 非表示にするユーザーID
     * @param accountName サインインしているアカウント名
     * @param rks アカウント名とrkクッキーに対応する認証情報rks
     */
    @FormUrlEncoded
    @POST("{account}/api.ignore.json")
    suspend fun __ignoreUser(
        @Field("username") user: String,
        @Path("account") accountName: String,
        @Field("rks") rks: String
    )

    /**
     * ユーザーを非表示にする
     */
    suspend fun ignoreUser(user: String)
}

/**
 * 外部向けのインターフェイスの実装部分
 */
class VerifiedAccountServiceImpl(
    body : VerifiedAccountService
) : VerifiedAccountService by body {
    internal lateinit var accountName : String

    internal lateinit var rks : String

    // ------ //

    /**
     * 非表示ユーザーリスト全件取得(公式設定ページの表示順)
     *
     * 途中で失敗した場合，最後に成功したところまでのリストとカーソルを返す
     */
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun getIgnoredUsersAll(): IgnoredUsersResponse {
        var cursor: String? = null
        val users = buildList {
            do {
                val result = runCatching {
                    getIgnoredUsers(limit = null, cursor = cursor)
                }.onSuccess {
                    cursor = it.cursor
                    addAll(it.users)
                }.onFailure {
                    it.printStackTrace()
                }
            } while (result.isSuccess && cursor != null)
        }
        return IgnoredUsersResponse(users = users, cursor = cursor)
    }

    /**
     * ユーザーを非表示にする
     *
     * @param user 非表示にするユーザーID
     */
    override suspend fun ignoreUser(user: String) {
        __ignoreUser(user, accountName, rks)
    }
}