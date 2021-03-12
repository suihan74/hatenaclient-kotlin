package com.suihan74.hatena.extension

import org.junit.Assert.*
import org.junit.Test
import java.net.URI

class URIKtTest {
    @Test
    fun queryParameters() {
        val uri = URI.create("https://localhost/?aaa=bbb&ccc=ddd&e=f")
        val params = uri.queryParameters
        assertEquals("bbb", params["aaa"])
        assertEquals("ddd", params["ccc"])
        assertEquals("f", params["e"])
    }

    @Test
    fun singleQueryParameter() {
        val uri = URI.create("https://localhost/?aaa=bbb")
        val params = uri.queryParameters
        assertEquals("bbb", params["aaa"])
    }

    @Test
    fun emptyQueryParameters() {
        val uri = URI.create("https://localhost/")
        val params = uri.queryParameters
        assertEquals(0, params.size)
    }

    @Test
    fun nonPairQueryParameters() {
        val uri = URI.create("https://localhost/?hoge")
        val params = uri.queryParameters
        assertEquals(1, params.size)
        assertEquals("", params["hoge"])
    }
}