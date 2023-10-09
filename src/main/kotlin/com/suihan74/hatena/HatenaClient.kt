package com.suihan74.hatena

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.suihan74.hatena.model.account.Account
import com.suihan74.hatena.api.*
import com.suihan74.hatena.exception.HatenaException
import com.suihan74.hatena.service.*
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

    /** アカウント関係のサービス */
    abstract val user : AccountService

    /** ブクマ関係のサービス */
    abstract val bookmark : BookmarkService

    /** エントリ関係のAPI */
    abstract val entry : EntryService

    /** スター関係のAPI */
    abstract val star : StarService

    /** Jsonパース時にコード側で取り扱っていないキーを無視する */
    protected val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    protected fun retrofitBuilder(baseUrl: String) : Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ObjectParameterConverterFactory)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)

    protected val retrofitForBookmark : Retrofit by lazy { retrofitBuilder(baseUrlB).build() }

    protected val retrofitForStar : Retrofit by lazy { retrofitBuilder(baseUrlS).build() }

    // ------ //

    /**
     * 指定URLのエントリURLを取得する
     *
     * @return "https://b.hatena.ne.jp/entry/..."
     */
    fun getEntryUrl(url: String) = buildString {
        append(HatenaClientBase.baseUrlB, "entry/")
        val schemeHttps = "https://"
        if (url.startsWith(schemeHttps)) append("s/", url.substring(schemeHttps.length))
        else append(url.substring(schemeHttps.length - 1))
    }
}

// ------ //

/**
 * サインイン無しで使用できるAPI群(mockテスト用途)
 */
abstract class HatenaClientBaseNoCertificationRequired : HatenaClientBase() {
    override val okHttpClient = OkHttpClient.Builder()
/*        .apply { interceptors().add(Interceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13")
                .build()
            return@Interceptor chain.proceed(request)
        }) }*/
        .build()

    // ------ //

    /** アカウント関係のサービス */
    override val user : AccountService by lazy {
        AccountService(api = retrofitForBookmark.create(AccountAPI::class.java))
    }

    /** ブクマ関係のサービス */
    override val bookmark : BookmarkService by lazy {
        BookmarkService(api = retrofitForBookmark.create(BookmarkAPI::class.java))
    }

    /** エントリ関係のサービス */
    override val entry : EntryService by lazy {
        EntryService(api = retrofitForBookmark.create(EntryAPI::class.java))
    }

    /** スター関係のサービス */
    override val star : StarService by lazy {
        StarService(api = retrofitForStar.create(StarAPI::class.java))
    }

    // ------ //



    // ------ //

    /** Cookieを利用して再ログイン */
    abstract suspend fun signIn(rk: String) : CertifiedHatenaClient
}

/**
 * サインイン無しで使用できるAPI群
 */
object HatenaClient : HatenaClientBaseNoCertificationRequired() {
    /** Cookieを利用して再ログイン */
    override suspend fun signIn(rk: String) = CertifiedHatenaClient.createInstance(rk)

    // ------ //

    @OptIn(ExperimentalSerializationApi::class)
    private val generalAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)
            .build()
            .create(GeneralAPI::class.java)
    }

    /** その他通信用サービス */
    internal val generalService by lazy { GeneralService(api = this.generalAPI) }
}

// ------ //

/**
 * サインイン状態で使用できるAPI群
 */
class CertifiedHatenaClient internal constructor() : HatenaClientBase() {
    val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    /** 認証情報(サインイン済みのとき値が入る。それ以外ではnull) */
    private val rk : HttpCookie?
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
    lateinit var accountName : String
        private set

    /** リクエストに加える認証情報 */
    private lateinit var rks : String

    /** アカウント情報を初期化 */
    internal fun initializeAccount(account: com.suihan74.hatena.model.account.Account, starRks: String) {
        accountName = account.name
        rks = account.rks
        userApi.also {
            it.accountName = accountName
            it.rks = rks
        }
        entryApi.also {
            it.accountName = accountName
            it.generalService = generalService
        }
        bookmarkApi.also {
            it.accountName = accountName
            it.rks = rks
            it.generalService = generalService
        }
        starApi.also {
            it.accountName = accountName
            it.rks = starRks
            it.rk = rkStr!!
        }
    }

    // ------ //

    /** アカウント関係のAPI */
    private val userApi = CertifiedAccountAPIImpl(retrofitForBookmark.create(CertifiedAccountAPI::class.java))

    /** ブクマ関係のAPI */
    private val bookmarkApi = CertifiedBookmarkAPIImpl(retrofitForBookmark.create(CertifiedBookmarkAPI::class.java))

    /** エントリ関係のAPI */
    private val entryApi = CertifiedEntryAPIImpl(retrofitForBookmark.create(CertifiedEntryAPI::class.java))

    /** スター関係のAPI */
    private val starApi = CertifiedStarAPIImpl(retrofitForStar.create(CertifiedStarAPI::class.java))

    @OptIn(ExperimentalSerializationApi::class)
    private val generalAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)
            .build()
            .create(GeneralAPI::class.java)
    }

    // ------ //

    /** アカウント関係のサービス */
    override val user by lazy { CertifiedAccountService(api = userApi) }

    /** ブクマ関係のサービス */
    override val bookmark by lazy { CertifiedBookmarkService(api = bookmarkApi) }

    /** エントリ関係のサービス */
    override val entry by lazy { CertifiedEntryService(api = entryApi) }

    /** スター関係のAPI */
    override val star by lazy { CertifiedStarService(api = starApi) }

    /** その他通信用サービス */
    internal val generalService by lazy { GeneralService(api = generalAPI) }

    // ------ //

    companion object {
        /**
         * 認証情報rkで再サインインして認証済みクライアントを作成する
         *
         * @throws HatenaClient 通信失敗
         */
        suspend fun createInstance(rk: String) = CertifiedHatenaClient().also {
            runCatching {
                it.cookieManager.cookieStore.add(
                    URI.create(baseUrlW),
                    HttpCookie("rk", rk).also { cookie ->
                        cookie.domain = ".hatena.ne.jp"
                        cookie.path = "/"
                        cookie.maxAge = -1
                    }
                )
                val account = it.user.getAccount()
                val starCredential = it.star.getCredential()
                it.initializeAccount(account, starCredential.rks!!)
            }.onFailure {
                throw HatenaException(cause = it)
            }
        }
    }
}