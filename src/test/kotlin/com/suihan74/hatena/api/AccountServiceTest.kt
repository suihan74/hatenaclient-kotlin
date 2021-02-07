package com.suihan74.hatena.api

import com.suihan74.hatena.account.IgnoredUsersResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

internal class AccountServiceTest : AccountServiceTestCredentials() {
    @Test
    fun signIn() = runBlocking {
        // Basic認証
        val client = HatenaClient.signIn(user, password)
        val rk = client.rk
        assert(rk != null)

        // アカウント情報取得
        val account = client.user.getAccount()
        Assert.assertEquals(user, account.name)
        println(Json.encodeToString(account))

        val rkStr = client.rkStr
        assert(rkStr != null)
        println("rk = $rkStr")

        // 再サインイン
        val client2 = HatenaClient.signIn(rkStr!!)
        val account2 = client2.user.getAccount()
        Assert.assertEquals(user, account2.name)
    }

    @Test
    fun getIgnoredUsers() = runBlocking {
        val client = HatenaClient.signIn(rk)

        fun printResult(response: IgnoredUsersResponse) {
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
        val client = HatenaClient.signIn(rk)

        fun printResult(response: IgnoredUsersResponse) {
            assert(response.users.isNotEmpty())
            println("size = " + response.users.size + " | cursor = " + response.cursor)
            response.users.forEach { println(it) }
        }

        client.user.getIgnoredUsersAll().let { response ->
            printResult(response)
        }
    }
}