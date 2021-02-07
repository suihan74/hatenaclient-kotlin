package com.suihan74.hatena.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.suihan74.hatena.account.Account
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpCookie
import java.net.URI

/**
 * はてなクライアントベースクラス
 */
sealed class HatenaClientBase {
    companion object {
        /** 主にアカウント関係のAPIのベースURL */
        const val baseUrlW = "https://www.hatena.ne.jp/"

        /** はてなブックマークのベースURL */
        const val baseUrlB = "https://b.hatena.ne.jp/"

        /** はてなスターのベースURL */
        const val baseUrlS = "https://s.hatena.ne.jp/"
    }

    /** 主にアカウント関係のAPIのベースURL */
    val baseUrlW = Companion.baseUrlW

    /** はてなブックマークのベースURL */
    val baseUrlB = Companion.baseUrlB

    /** はてなスターのベースURL */
    val baseUrlS = Companion.baseUrlS

    // ------ //

    protected abstract val okHttpClient : OkHttpClient

    // ------ //

    /** アカウント関係のAPI */
    abstract val user : AccountService

    /** ブクマ関係のAPI */
    abstract val bookmark : BookmarkService

    /** エントリ関係のAPI */
    abstract val entry : EntryService

    @OptIn(ExperimentalSerializationApi::class)
    protected fun retrofitBuilder(baseUrl: String) =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)

    protected val retrofitForBookmark by lazy { retrofitBuilder(baseUrlB).build() }

    protected val retrofitForStar by lazy { retrofitBuilder(baseUrlS).build() }
}

// ------ //

/**
 * サインイン無しで使用できるAPI群(mockテスト用途)
 */
abstract class HatenaClientBaseNoCertificationRequired : HatenaClientBase() {
    override val okHttpClient by lazy {
        OkHttpClient.Builder()
            .build()
    }

    // ------ //

    /** アカウント関係のAPI */
    override val user : AccountService by lazy { retrofitForBookmark.create(AccountService::class.java) }

    /** ブクマ関係のAPI */
    override val bookmark : BookmarkService by lazy { retrofitForBookmark.create(BookmarkService::class.java) }

    /** エントリ関係のAPI */
    override val entry : EntryService by lazy { retrofitForBookmark.create(EntryService::class.java) }

    // ------ //

    /** サインインが必要なAPIが使用できるインスタンスを作成する */
    abstract suspend fun signIn(name: String, password: String) : CertifiedHatenaClient

    /** Cookieを利用して再ログイン */
    abstract suspend fun signIn(rk: String) : CertifiedHatenaClient
}

/**
 * サインイン無しで使用できるAPI群
 */
object HatenaClient : HatenaClientBaseNoCertificationRequired() {
    /** サインインが必要なAPIが使用できるインスタンスを作成する */
    override suspend fun signIn(name: String, password: String) = CertifiedHatenaClient.createInstance(name, password)

    /** Cookieを利用して再ログイン */
    override suspend fun signIn(rk: String) = CertifiedHatenaClient.createInstance(rk)
}

// ------ //

/**
 * サインイン状態で使用できるAPI群
 */
class CertifiedHatenaClient internal constructor() : HatenaClientBase() {
    val cookieManager by lazy {
        CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
    }

    /** 認証情報(サインイン済みのとき値が入る。それ以外ではnull) */
    val rk : HttpCookie?
        get() = cookieManager.cookieStore.cookies.firstOrNull {
            it.domain == ".hatena.ne.jp" && it.name == "rk"
        }

    /** rkの値 */
    val rkStr : String?
        get() = rk?.value

    /** Cookieを利用するHttpClient */
    override val okHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

    /** サインインしているアカウント情報 */
    private lateinit var accountName : String

    /** リクエストに加える認証情報 */
    private lateinit var rks : String

    /** アカウント情報を初期化 */
    internal fun initializeAccount(account: Account) {
        accountName = account.name
        rks = account.rks
        (user as CertifiedAccountServiceImpl).let {
            it.accountName = accountName
            it.rks = rks
        }
    }

    // ------ //

    /** アカウント関係のAPI */
    override val user : CertifiedAccountService by lazy {
        CertifiedAccountServiceImpl(retrofitForBookmark.create(CertifiedAccountService::class.java))
    }

    /** ブクマ関係のAPI */
    override val bookmark : BookmarkService by lazy { retrofitForBookmark.create(BookmarkService::class.java) }

    /** エントリ関係のAPI */
    override val entry : CertifiedEntryService by lazy { retrofitForBookmark.create(CertifiedEntryService::class.java) }

    // ------ //

    companion object {
        /**
         * ユーザーIDとパスワードでサインインして認証済みクライアントを作成する
         */
        suspend fun createInstance(name: String, password: String) = CertifiedHatenaClient().also {
            it.user.__signInImpl(name, password)
            it.user.getAccount().let { account ->
                it.initializeAccount(account)
            }
        }
        /**
         * 認証情報rkで再サインインして認証済みクライアントを作成する
         */
        suspend fun createInstance(rk: String) = CertifiedHatenaClient().also {
            it.cookieManager.cookieStore.add(
                URI.create(baseUrlW),
                HttpCookie("rk", rk).also { cookie ->
                    cookie.domain = ".hatena.ne.jp"
                    cookie.path = "/"
                    cookie.maxAge = -1
                }
            )
            it.user.getAccount().let { account ->
                it.initializeAccount(account)
            }
        }
    }
}