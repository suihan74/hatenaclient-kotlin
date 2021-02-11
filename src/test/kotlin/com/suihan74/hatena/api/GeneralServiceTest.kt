package com.suihan74.hatena.api

import com.suihan74.hatena.exception.InvalidResponseException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import com.suihan74.hatena.exception.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

class GeneralServiceTest {
    private class MockGeneralService(
        private val delegate : BehaviorDelegate<GeneralService>
    ) : GeneralService {
        var body : String = ""

        var exception : Throwable? = null

        var mediaType : MediaType = "text/plain".toMediaType()

        override suspend fun get(url: String): Response<ResponseBody> {
            exception?.let { throw it }
            return delegate.returningResponse(
                body.toResponseBody(mediaType)
            ).get(url)
        }
    }

    // ------ //

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .build()

    private val mockService = MockGeneralService(
        MockRetrofit.Builder(retrofit)
            .networkBehavior(NetworkBehavior.create())
            .build()
            .create(GeneralService::class.java)
    )

    // ------ //

    @Before
    fun init() {
        mockService.body = ""
        mockService.exception = null
    }

    @Test
    fun get() = runBlocking {
        mockService.body = "hello"

        val response = mockService.get("https://foo.bar.baz/")
        response.body()?.use { body ->
            val content = body.string()
            println(response)
            println(content)
            assertEquals("hello", content)
        } ?: fail()
    }

    @Test
    fun get_not_found() = runBlocking {
        mockService.exception = HttpException(
            Response.error<ResponseBody>(404, "".toResponseBody(mockService.mediaType))
        )

        runCatching {
            val response = mockService.get("https://foo.bar.baz/")
            response.body()?.use { body ->
                val content = body.string()
                println(response)
                println(content)
                assertEquals("hello", content)
            }
        }.onSuccess {
            fail()
        }.onFailure {
            println(it.stackTraceToString())
        }

        Unit
    }

    @Test
    fun getHtml() = runBlocking {
        mockService.body = """
            <!DOCTYPE html>
            <html>
                <head></head>
                <body></body>
            </html>
        """.trimIndent()

        mockService.getHtml("https://localhost/") { doc ->
            val html = doc.getElementsByTag("html").first()
            val head = html.getElementsByTag("head").first()
            val body = html.getElementsByTag("body").first()
        }
    }

    @Test
    fun getHtml_not_found() = runBlocking {
        mockService.exception = HttpException(
            Response.error<ResponseBody>(404, "".toResponseBody(mockService.mediaType))
        )

        runCatching {
            mockService.getHtml("https://localhost/") { doc ->
                val html = doc.getElementsByTag("html").first()
                val head = html.getElementsByTag("head").first()
                val body = html.getElementsByTag("body").first()
            }
        }.onSuccess {
            fail()
        }.onFailure {
            println(it.stackTraceToString())
        }

        Unit
    }

    @Test
    fun getHtml_invalid_response() = runBlocking {
        mockService.body = """
            <!DOCTYPE html>
            <html>
                <head><head>
                <body></body>
            </html>
        """.trimIndent()

        runCatching {
            mockService.getHtml("https://localhost/") { doc ->
                val html = doc.getElementsByTag("html").first()
                val head = html.getElementsByTag("head").first()
                val body = html.getElementsByTag("body").first()

                // element not found
                val content = body.getElementById("content")!!
            }
        }.onSuccess {
            fail()
        }.onFailure {
            assert(it is InvalidResponseException)
            println(it.stackTraceToString())
        }

        Unit
    }
}