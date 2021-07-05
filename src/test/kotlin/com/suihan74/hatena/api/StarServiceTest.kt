package com.suihan74.hatena.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.ZoneOffset

class StarServiceTest : AccountServiceTestCredentials() {
    private var client : CertifiedHatenaClient

    init {
        runBlocking {
            client = HatenaClient.signIn(rk)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val service = Retrofit.Builder()
        .baseUrl(HatenaClient.baseUrlS)
        .addConverterFactory(
            Json.asConverterFactory("application/json".toMediaType())
        )
        .client(OkHttpClient())
        .build()
        .create(StarService::class.java)

    @Test
    fun getStarsEntry() = runBlocking {
        val url = "https://b.hatena.ne.jp/suihan74/20200805#bookmark-4689504406681638082"
        val entry = service.getStarsEntry(url)
        println("total : " + entry.totalCount)
        entry.allStars.forEach {
            println(it.user + " : " + it.color.name + "(" + it.count + ")")
        }
    }

    @Test
    fun getStarsEntries() = runBlocking {
        val urls = listOf(
            "https://b.hatena.ne.jp/suihan74/20200805#bookmark-4689504406681638082",
            "https://b.hatena.ne.jp/suihan74/20201120#bookmark-4694479151298190594"
        )
        val entries = service.getStarsEntries(urls)
        entries.forEach { entry ->
            println(entry.url)
            println("total : " + entry.totalCount)
            entry.allStars.forEach {
                println(it.user + " : " + it.color.name + "(" + it.count + ")")
            }
        }
    }

    @Test
    fun signInStar() = runBlocking {
        val response = client.star.__getCredential()
        println(Json.encodeToString(response))
    }

    @Test
    fun getMyRecentStars() = runBlocking {
        val entries = client.star.getMyRecentStars()
        entries.forEach {
            println(Json.encodeToString(it))
        }
    }

    @Test
    fun getRecentStarsReport() = runBlocking {
        val response = client.star.getRecentStarsReport()
        response.entries.forEach {
            println(Json.encodeToString(it))
        }
    }

    @Test
    fun getMyColorStars() = runBlocking {
        val response = client.star.getMyColorStars()
        val colorStars = response.stars
        println(Json.encodeToString(colorStars))
    }

    @Test
    fun getColorPalette() = runBlocking {
        val response = client.star.getColorPalette(
            url = "https://b.hatena.ne.jp/",
            date = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )
        println(Json.encodeToString(response))
    }
}