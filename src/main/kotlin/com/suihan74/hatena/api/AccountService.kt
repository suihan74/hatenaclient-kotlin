package com.suihan74.hatena.api

import com.suihan74.hatena.account.*
import com.suihan74.hatena.account.Tag
import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.extension.toUserIconUrl
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
    @Deprecated("")
    @FormUrlEncoded
    @POST("${HatenaClientBase.baseUrlW}login")
    suspend fun __signInImpl(
        @Field("name") name: String,
        @Field("password") password: String,
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

    // ------ //

    /**
     * 指定ユーザーが使用したタグを取得する
     */
    @GET("{user}/tags.json")
    suspend fun __getUserTags(@Path("user") user: String) : TagsResponse
}

/**
 * ユーザーのアイコンURLを取得する
 */
fun AccountService.getUserIconUrl(user: String) : String = user.toUserIconUrl

/**
 * 指定ユーザーが使用したタグを取得する
 */
suspend fun AccountService.getUserTags(user: String) : List<Tag> {
    val response = __getUserTags(user)
    return response.tags
        .map { Tag(text = it.key, index = it.value.index, count = it.value.count, timestamp = it.value.timestamp) }
        .sortedBy { it.index }
}

// ------ //

/**
 * 要認証のアカウント関係API
 */
interface CertifiedAccountService : AccountService {
    val accountName : String

    val rks : String

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
    suspend fun readNotices(
        @Field("rks") rks: String = this.rks
    ) : ReadNoticesResponse

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
    suspend fun ignoreUser(
        @Field("username") user: String,
        @Path("account") accountName: String = this.accountName,
        @Field("rks") rks: String = this.rks
    ) : UnitResponse

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
    suspend fun unIgnoreUser(
        @Field("username") user: String,
        @Path("account") accountName: String = this.accountName,
        @Field("rks") rks: String = this.rks
    ) : UnitResponse
}

// ------- //

class CertifiedAccountServiceImpl(delegate : CertifiedAccountService) : CertifiedAccountService by delegate {
    override lateinit var accountName: String
    override lateinit var rks: String
}

// ------- //

/**
 * 非表示ユーザーリストを全件取得
 *
 * @throws HttpException 通信失敗
 */
suspend fun CertifiedAccountService.getIgnoredUsersAll() : IgnoredUsersResponse {
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
 * ログインユーザーが使用しているタグを取得する
 */
suspend fun CertifiedAccountService.getUserTags() : List<Tag> {
    return getUserTags(user = accountName)
}
