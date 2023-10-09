package com.suihan74.hatena.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.model.account.IgnoredUsersResponse
import com.suihan74.hatena.api.CertifiedAccountAPI
import com.suihan74.hatena.api.CertifiedAccountAPIImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/*
 * テストの実行にはテストユーザーの認証情報が必要になります
 * 以下のような内容の抽象クラスを用意してください
 */
/*
abstract class AccountServiceTestCredentials {
    protected val user = "suihan74"
    protected val password = "hogehoge"
    protected val rk = "foobarbaz"
}
*/

internal class AccountServiceTest : AccountServiceTestCredentials() {
    private val client = runBlocking { HatenaClient.signIn(rk) }

    @Test
    fun signIn() = runBlocking {
        // Basic認証
        /*
        // 2021.06 公式が reCAPTCHA 導入してできなくなった
        val client = HatenaClient.signIn(user, password)
        val rk = client.rk
        assert(rk != null)
        */

        // アカウント情報取得
        val account = client.user.getAccount()
        assertEquals(user, account.name)
        println(Json.encodeToString(account))

        val rkStr = client.rkStr
        assert(rkStr != null)
        println("rk = $rkStr")

        // 再サインイン
        val client2 = HatenaClient.signIn(rkStr!!)
        val account2 = client2.user.getAccount()
        assertEquals(user, account2.name)
    }

    @Test
    fun getAccount() = runBlocking {
        val account = client.user.getAccount()
        assertEquals(user, account.name)
        println(Json.encodeToString(account))
    }

    @Test
    fun getIgnoredUsers() = runBlocking {
        fun printResult(response: com.suihan74.hatena.model.account.IgnoredUsersResponse) {
            assert(response.users.isNotEmpty())
            println("size = " + response.users.size + " | cursor = " + response.cursor)
            response.users.forEach { println(it) }
        }

        client.user.getIgnoredUsers().let { response ->
            printResult(response)
        }

        println("======")

        client.user.getIgnoredUsers(limit = 0).let { response ->
            printResult(response)
        }

        println("======")

        client.user.getIgnoredUsers(limit = 1).let { response ->
            printResult(response)
        }

        println("======")

        client.user.getIgnoredUsers(limit = -1).let { response ->
            printResult(response)
        }
    }

    @Test
    fun getIgnoredUsersAll() = runBlocking {
        fun printResult(response: com.suihan74.hatena.model.account.IgnoredUsersResponse) {
            assert(response.users.isNotEmpty())
            println("size = " + response.users.size + " | cursor = " + response.cursor)
            response.users.forEach { println(it) }
        }

        client.user.getIgnoredUsersAll().let { response ->
            printResult(response)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun getIgnoredUsersAll_fails_first_trial() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(404))
        }
        server.start()

        val mockApi = CertifiedAccountAPIImpl(Retrofit.Builder()
            .baseUrl(server.url(""))
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .client(OkHttpClient())
            .build()
            .create(CertifiedAccountAPI::class.java)
        )
        val mockService = CertifiedAccountService(api = mockApi)

        runCatching {
            mockService.getIgnoredUsersAll()
        }.onSuccess {
            fail()
        }.onFailure {
            it.printStackTrace()
        }

        server.shutdown()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun getIgnoredUsersAll_fails_on_the_way() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200).setBody("""
                {"users":["test0","test1"],"cursor":"cursor0"}
                """.trimIndent()))
            enqueue(MockResponse().setResponseCode(404))
            enqueue(MockResponse().setResponseCode(200).setBody("""
                {"users":["test2","test3"],"cursor":null}
                """.trimIndent()))
        }
        server.start()

        val mockApi = CertifiedAccountAPIImpl(Retrofit.Builder()
            .baseUrl(server.url(""))
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .client(OkHttpClient())
            .build()
            .create(CertifiedAccountAPI::class.java)
        )
        val mockService = CertifiedAccountService(api = mockApi)

        runCatching {
            mockService.getIgnoredUsersAll()
        }.onSuccess {
            println(it.users)
        }.onFailure {
            it.printStackTrace()
            fail()
        }

        server.shutdown()
    }

    @Test
    fun ignoreUser(): Unit = runBlocking {
//        client.user.ignoreUser("")
//        client.user.unIgnoreUser("")
    }

    @Test
    fun ignoreUser_not_existed_user() = runBlocking {
        runCatching {
            client.user.ignoreUser("!______unknownuser")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            fail()
        }

        runCatching {
            client.user.unIgnoreUser("!______unknownuser")
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            fail()
        }

        return@runBlocking
    }

    @Test
    fun getFollowings() = runBlocking {
        client.user.getFollowings(client.accountName).users.forEach {
            println(it.name)
        }
    }

    @Test
    fun getFollowers() = runBlocking {
        client.user.getFollowers(client.accountName).users.forEach {
            println(it.user)
        }
    }

    @Test
    fun getNotices() = runBlocking {
        val response = client.user.getNotices()
        println(response.status)
        println(
            LocalDateTime.ofInstant(response.lastSeen, ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_DATE_TIME)
        )
        response.notices.forEach {
            println(Json.encodeToString(it))
        }
    }

    @Test
    fun readNotices() = runBlocking {
        val response = client.user.readNotices()
        println("status = ${response.status}")
        println("lastSeen = " +
            response.lastSeen.atOffset(ZoneOffset.ofHours(9))
            .format(DateTimeFormatter.ISO_DATE_TIME)
        )
    }

    @Test
    fun getUserIconUrl() {
        val url = HatenaClient.user.getUserIconUrl("suihan74")
        assertEquals("https://cdn1.www.st-hatena.com/users/suihan74/profile.gif", url)
    }

    @Test
    fun getUserTags() = runBlocking {
        val list = HatenaClient.user.getUserTags("suihan74")
        list.forEach { tag ->
            println("${tag.text}(${tag.count}) : ${tag.timestamp}")
        }
    }

    @Test
    fun getSignedUserTags() = runBlocking {
        val list = client.user.getUserTags()
        list.forEach { tag ->
            println("${tag.text}(${tag.count}) : ${tag.timestamp}")
        }
    }
}
