package com.suihan74.hatena.model.entry

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class EntryTest {
    @Test
    fun entryUrlHttps() {
        val url = "https://foo.bar.baz/"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN)
        assertEquals(url, entry.url)
        assertEquals("https://b.hatena.ne.jp/entry/s/foo.bar.baz/", entry.entryUrl)
    }

    @Test
    fun entryUrlHttp() {
        val url = "http://foo.bar.baz/"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN)
        assertEquals(url, entry.url)
        assertEquals("https://b.hatena.ne.jp/entry/foo.bar.baz/", entry.entryUrl)
    }

    @Test
    fun entryUrlSet() {
        val url = "https://foo.bar.baz/"
        val entryUrl = "https://b.hatena.ne.jp/entry/s/foo.bar.baz/"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN, _entryUrl = entryUrl)
        assertEquals(url, entry.url)
        assertEquals(entryUrl, entry.entryUrl)
    }

    @Test
    fun rootUrl() {
        val url = "https://foo.bar.baz/hoge"
        val expected = "https://foo.bar.baz/"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN)
        assertEquals(url, entry.url)
        assertEquals(expected, entry.rootUrl)
    }

    @Test
    fun rootUrlSet() {
        val url = "https://foo.bar.baz/hoge/fuga"
        val rootUrl = "https://foo.bar.baz/hoge/"
        val expected = "https://foo.bar.baz/hoge/"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN, _rootUrl = rootUrl)
        assertEquals(url, entry.url)
        assertEquals(expected, entry.rootUrl)
    }

    @Test
    fun imageUrlNoImage() {
        val url = "https://foo.bar.baz/hoge/fuga"
        val expected = Entry.IMAGE_URL_NO_IMAGE
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN)
        assertEquals(expected, entry.imageUrl)
    }

    @Test
    fun imageUrlSet() {
        val url = "https://foo.bar.baz/hoge/fuga"
        val imageUrl = "https://foo.bar.baz/image.png"
        val expected = "https://foo.bar.baz/image.png"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN, _imageUrl = imageUrl)
        assertEquals(expected, entry.imageUrl)
    }

    @Test
    fun faviconUrlTemp() {
        val url = "https://foo.bar.baz/hoge/fuga"
        val expected = Entry.TEMP_FAVICON_URL_BASE + "foo.bar.baz"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN)
        assertEquals(expected, entry.faviconUrl)
    }

    @Test
    fun faviconUrlSet() {
        val url = "https://foo.bar.baz/hoge/fuga"
        val faviconUrl = "https://foo.bar.baz/favicon.png"
        val expected = "https://foo.bar.baz/favicon.png"
        val entry = EntryItem("", url, 0, "", 0, Instant.MIN, _faviconUrl = faviconUrl)
        assertEquals(expected, entry.faviconUrl)
    }
}