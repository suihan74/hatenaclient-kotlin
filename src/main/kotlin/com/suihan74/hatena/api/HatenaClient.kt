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

    /** アカウント関係のAPI */
    override val user : AccountService = retrofitForBookmark.create(AccountService::class.java)

    /** ブクマ関係のAPI */
    override val bookmark : BookmarkService = retrofitForBookmark.create(BookmarkService::class.java)

    /** エントリ関係のAPI */
    override val entry : EntryService = retrofitForBookmark.create(EntryService::class.java)

    /** スター関係のAPI */
    override val star : StarService = retrofitForStar.create(StarService::class.java)

    // ------ //

    /** サインインが必要なAPIが使用できるインスタンスを作成する */
    @Deprecated("")
    abstract suspend fun signIn(name: String, password: String) : CertifiedHatenaClient

    /** Cookieを利用して再ログイン */
    abstract suspend fun signIn(rk: String) : CertifiedHatenaClient
}

/**
 * サインイン無しで使用できるAPI群
 */
object HatenaClient : HatenaClientBaseNoCertificationRequired() {
    /** サインインが必要なAPIが使用できるインスタンスを作成する */
    @Deprecated("", replaceWith = ReplaceWith("signIn(rk: String)"))
    override suspend fun signIn(name: String, password: String) = CertifiedHatenaClient.createInstance(name, password)

    /** Cookieを利用して再ログイン */
    override suspend fun signIn(rk: String) = CertifiedHatenaClient.createInstance(rk)

    // ------ //

    @OptIn(ExperimentalSerializationApi::class)
    internal val generalService by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)
            .build()
            .create(GeneralService::class.java)
    }
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
    internal fun initializeAccount(account: Account, starRks: String) {
        accountName = account.name
        rks = account.rks
        (user as CertifiedAccountServiceImpl).let {
            it.accountName = accountName
            it.rks = rks
        }
        (entry as CertifiedEntryServiceImpl).let{
            it.accountName = accountName
            it.generalService = generalService
        }
        (bookmark as CertifiedBookmarkServiceImpl).let {
            it.accountName = accountName
            it.rks = rks
            it.generalService = generalService
        }
        (star as CertifiedStarServiceImpl).let {
            it.accountName = accountName
            it.rks = starRks
            it.rk = rkStr!!
        }
    }

    // ------ //

    /** アカウント関係のAPI */
    override val user : CertifiedAccountService =
        CertifiedAccountServiceImpl(retrofitForBookmark.create(CertifiedAccountService::class.java))

    /** ブクマ関係のAPI */
    override val bookmark : CertifiedBookmarkService =
        CertifiedBookmarkServiceImpl(retrofitForBookmark.create(CertifiedBookmarkService::class.java))

    /** エントリ関係のAPI */
    override val entry : CertifiedEntryService =
        CertifiedEntryServiceImpl(retrofitForBookmark.create(CertifiedEntryService::class.java))

    /** スター関係のAPI */
    override val star : CertifiedStarService =
        CertifiedStarServiceImpl(retrofitForStar.create(CertifiedStarService::class.java))

    // ------ //

    @OptIn(ExperimentalSerializationApi::class)
    internal val generalService by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)
            .build()
            .create(GeneralService::class.java)
    }

    // ------ //

    companion object {
        /**
         * ユーザーIDとパスワードでサインインして認証済みクライアントを作成する
         */
        @Deprecated("")
        suspend fun createInstance(name: String, password: String) = CertifiedHatenaClient().also {
            it.user.__signInImpl(name, password)
            it.user.getAccount().let { account ->
                it.initializeAccount(account, "")
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
            val account = it.user.getAccount()
            val starCredential = it.star.__getCredential()
            it.initializeAccount(account, starCredential.rks!!)
        }
    }
}