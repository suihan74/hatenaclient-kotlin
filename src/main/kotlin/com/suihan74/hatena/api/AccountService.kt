package com.suihan74.hatena.api

import com.suihan74.hatena.account.*
import com.suihan74.hatena.exception.HttpException
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
     * @throws HttpException 通信失敗
     */
    @FormUrlEncoded
    @POST("${HatenaClientBase.baseUrlW}login")
    suspend fun __signInImpl(
        @Field("name") name: String,
        @Field("password") password: String
    ) : Response<ResponseBody>

    /**
     * 指定ユーザーがフォローしているユーザーリストを取得する
     *
     * @param user 対象ユーザーID
     */
    @GET("{user}/follow.json")
    suspend fun getFollowings(
        @Path("user") user: String
    ) : FollowingsResponse

    /**
     * 指定ユーザーをフォローしているユーザーリストを取得する
     *
     * @param user 対象ユーザーID
     */
    @GET("api/internal/cambridge/user/{user}/followers")
    suspend fun getFollowers(
        @Path("user") user: String
    ) : FollowersResponse
}

// ------ //

/**
 * 要認証のアカウント関係API
 */
interface CertifiedAccountService : AccountService {
    /**
     * アカウント情報を取得
     *
     * @throws HttpException 通信失敗
     */
    @GET("my.name")
    suspend fun getAccount() : Account

    // ------ //

    /**
     * 通知を取得する
     */
    @GET("${HatenaClientBase.baseUrlW}notify/api/pull")
    suspend fun getNotices() : NoticesResponse

    /**
     * 通知最終確認時刻を更新する
     */
    @FormUrlEncoded
    @POST("${HatenaClientBase.baseUrlW}notify/api/read")
    suspend fun __readNotices(
        @Field("url") rks: String
    ) : ReadNoticesResponse

    /**
     * 通知最終確認時刻を更新する
     */
    suspend fun readNotices() : ReadNoticesResponse

    // ------ //

    /**
     * 非表示ユーザーリストを取得
     *
     * @param limit 最大取得件数。`null`, `0`, 負値はすべて`null`として扱われ，適当な件数と追加取得用のカーソルが返される
     * @param cursor 順次取得用カーソル
     * @return 非表示ユーザーリスト(公式設定ページの表示順)とカーソルを含んだレスポンス
     * @throws HttpException 通信失敗
     */
    @GET("api/my/ignore_users")
    suspend fun getIgnoredUsers(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ) : IgnoredUsersResponse

    /**
     * 非表示ユーザーリストを全件取得
     *
     * @throws HttpException 通信失敗
     */
    suspend fun getIgnoredUsersAll() : IgnoredUsersResponse

    // ------ //

    /**
     * ユーザーを非表示にする
     *
     * @param user 非表示にするユーザーID
     * @param accountName サインインしているアカウント名
     * @param rks アカウント名とrkクッキーに対応する認証情報rks
     * @throws HttpException 通信失敗
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
     *
     * @param user 非表示を解除するユーザーID
     * @param accountName サインインしているアカウント名
     * @param rks アカウント名とrkクッキーに対応する認証情報rks
     * @throws HttpException code=500: ユーザーが存在しない
     * @throws HttpException 通信失敗
     */
    @FormUrlEncoded
    @POST("{account}/api.unignore.json")
    suspend fun __unIgnoreUser(
        @Field("username") user: String,
        @Path("account") accountName: String,
        @Field("rks") rks: String
    )

    /**
     * ユーザーを非表示にする
     *
     * 既に非表示設定済みでも成功する点には注意
     *
     * @throws HttpException code=500: ユーザーが存在しない
     * @throws HttpException 通信失敗
     */
    suspend fun ignoreUser(user: String)

    /**
     * ユーザーの非表示を解除する
     *
     * 既に非表示解除状態でも成功する点には注意
     *
     * @throws HttpException code=500: ユーザーが存在しない
     * @throws HttpException 通信失敗
     */
    suspend fun unIgnoreUser(user: String)
}

/**
 * 外部向けのインターフェイスの実装部分
 */
class CertifiedAccountServiceImpl(delegate : CertifiedAccountService) : CertifiedAccountService by delegate {
    internal lateinit var accountName : String

    internal lateinit var rks : String

    // ------ //

    /**
     * @see CertifiedAccountService.readNotices
     */
    override suspend fun readNotices() : ReadNoticesResponse {
        return __readNotices(rks)
    }

    // ------ //

    /**
     * @see CertifiedAccountService.getIgnoredUsersAll
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
                    // 初回で失敗した場合は例外送出
                    if (cursor == null) {
                        throw it
                    }
                }
            } while (result.isSuccess && cursor != null)
        }
        return IgnoredUsersResponse(users = users, cursor = cursor)
    }

    /**
     * @see CertifiedAccountService.ignoreUser
     */
    override suspend fun ignoreUser(user: String) = __ignoreUser(user, accountName, rks)

    /**
     * @see CertifiedAccountService.unIgnoreUser
     */
    override suspend fun unIgnoreUser(user: String) = __unIgnoreUser(user, accountName, rks)
}