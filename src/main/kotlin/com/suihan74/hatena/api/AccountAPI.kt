package com.suihan74.hatena.api

import com.suihan74.hatena.HatenaClientBase
import com.suihan74.hatena.exception.HttpException
import com.suihan74.hatena.model.account.NoticesResponse
import com.suihan74.hatena.model.account.ReadNoticesResponse
import com.suihan74.hatena.model.account.TagsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


/**
 * アカウント関係のAPI
 */
interface AccountAPI {
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
    ) : com.suihan74.hatena.model.account.FollowingsResponse

    /**
     * 指定ユーザーをフォローしているユーザーリストを取得する
     *
     * @param user 対象ユーザーID
     */
    @GET("api/internal/cambridge/user/{user}/followers")
    suspend fun getFollowers(
        @Path("user") user: String
    ) : com.suihan74.hatena.model.account.FollowersResponse

    // ------ //

    /**
     * 指定ユーザーが使用したタグを取得する
     */
    @GET("{user}/tags.json")
    suspend fun getUserTags(@Path("user") user: String) : TagsResponse
}

// ------ //

/**
 * 要認証のアカウント関係API
 */
interface CertifiedAccountAPI : AccountAPI {
    val accountName : String

    val rks : String

    /**
     * アカウント情報を取得
     *
     * @throws HttpException 通信失敗
     */
    @GET("my.name")
    suspend fun getAccount() : com.suihan74.hatena.model.account.Account

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
    ) : com.suihan74.hatena.model.account.IgnoredUsersResponse

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
    ) : Response<Unit>

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
    ) : Response<Unit>
}

// ------- //

internal class CertifiedAccountAPIImpl(delegate : CertifiedAccountAPI) : CertifiedAccountAPI by delegate {
    override lateinit var accountName: String
    override lateinit var rks: String
}
