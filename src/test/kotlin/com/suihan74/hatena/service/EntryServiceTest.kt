package com.suihan74.hatena.service

import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.model.entry.*
import com.suihan74.hatena.exception.HttpException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EntryServiceTest : AccountServiceTestCredentials() {
    private fun println(entry: Entry) {
        println("  title = " + entry.title)
        println("  url = " + entry.url)
        println("  entryUrl = " + entry.entryUrl)
        println("  rootUrl = " + entry.rootUrl)
        println("  imageUrl = " + entry.imageUrl)
        println("  faviconUrl = " + entry.faviconUrl)
        println(Json.encodeToString(entry))
        println("====================")
    }

    private fun println(info: MaintenanceEntry) {
        val offset = ZoneOffset.ofHours(9)
        println("  title = " + info.title)
        println("  url = " + info.url)
        println("  body = " + info.body)
        println("  resolved = " + info.resolved)
        println("  createdAt = " + OffsetDateTime.ofInstant(info.createdAt, offset))
        println("  updatedAt = " + OffsetDateTime.ofInstant(info.updatedAt, offset))
        println(Json.encodeToString(info))
        println("====================")
    }

    private suspend fun getEntries(entriesType: EntriesType, category: Category) {
        println("type = " + entriesType.name + " | category = " + category.name)
        HatenaClient.entry.getEntries(entriesType, category).let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach { println(it) }
        }
    }

    private suspend fun getEntries(entriesType: EntriesType, issue: Issue) {
        println("type = " + entriesType.name + " | issue = " + issue.name)
        HatenaClient.entry.getIssueEntries(entriesType, issue).entries.let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach { println(it) }
        }
    }

    // ------ //

    @Test
    fun testAllEntries() = runBlocking {
        Category.values().forEach {
            getEntries(EntriesType.HOT, it)
            getEntries(EntriesType.RECENT, it)
            println("=========================")
        }
    }

    @Test
    fun getBookmarkedEntries() = runBlocking {
        val client = HatenaClient.signIn(rk)
        client.entry.getBookmarkedEntries().let { entries ->
            entries.forEach { println(it) }
        }
    }

    @Test
    fun searchBookmarkedEntries() = runBlocking {
        val client = HatenaClient.signIn(rk)
        client.entry.searchBookmarkedEntries(
            SearchType.TAG,
            query = "あとで読む"
        ).let { entries ->
            entries.forEach { println(it) }
        }
    }

    @Test
    fun getUserBookmarkedEntries() = runBlocking {
        HatenaClient.entry.getBookmarkedEntries(
            user = "suihan74"
        ).forEach {
            println(it)
        }
    }

    @Test
    fun getMyHotEntries() = runBlocking {
        val client = HatenaClient.signIn(rk)
        client.entry.getMyHotEntries().let { entries ->
            entries.forEach { println(it) }
        }
    }

    @Test
    fun allIssues() = runBlocking {
        Category.values().forEach {
            println("===== " + it.name + " =====")
            runCatching {
                HatenaClient.entry.getIssues(it).issues.forEachIndexed { index, issue ->
                    println(issue.entry!!)

                    if (index == 0) {
                        getEntries(EntriesType.HOT, issue)
                        getEntries(EntriesType.RECENT, issue)
                    }
                }
            }.onFailure { e ->
                // `ALL`, `GENERAL`ではIssueが得られない
                e.printStackTrace()
                val cause = e.cause
                assert(
                    (it == Category.ALL || it == Category.GENERAL) &&
                        cause is HttpException && cause.code() == 400
                )
            }
        }
    }

    @Test
    fun getEntryId_success() = runBlocking {
        val url = "https://anond.hatelabo.jp/20210127175952"
        val id = HatenaClient.entry.getEntryId(url)
        val expected = 4697656063361718818
        assertEquals(expected, id)
        println("eid = $id : $url")
    }

    @Test
    fun getEntryId_not_existed() = runBlocking {
        val url = "https://b.hatena.ne.jp/entry/s/b.hatena.ne.jp/entry/s/anond.hatelabo.jp/20210127175952"
        runCatching {
            val id = HatenaClient.entry.getEntryId(url)
            println("eid = $id : $url")
        }.onSuccess {
            fail()
        }.onFailure {
            it.printStackTrace()
        }
        Unit
    }

    @Test
    fun getUrl() = runBlocking {
        val eid = 4698022722211478562
        val url = HatenaClient.entry.getUrl(eid)
        val expected = "https://suihan74.github.io/posts/2021/02_04_00_satena_160/"
        assertEquals(expected, url)
    }

    @Test
    fun getFaviconUrl() {
        val url = HatenaClient.entry.getFaviconUrl("https://suihan74.github.io/posts/2021/02_04_00_satena_160/")
        assertEquals(
            "https://cdn-ak2.favicon.st-hatena.com/?url=https%3A%2F%2Fsuihan74.github.io%2Fposts%2F2021%2F02_04_00_satena_160%2F",
            url
        )

        val url2 = HatenaClient.entry.getFaviconUrl("https://hoge.com/ほげ")
        assertEquals(
            "https://cdn-ak2.favicon.st-hatena.com/?url=https%3A%2F%2Fhoge.com%2F%E3%81%BB%E3%81%92",
            url2
        )
    }

    @Test
    fun searchEntries() = runBlocking {
        val entries = HatenaClient.entry.searchEntries(SearchType.TAG, "test", EntriesType.RECENT)
        entries.forEach {
            println(it.title)
        }
    }

    @Test
    fun searchEntriesWithDateRange() = runBlocking {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
        val offset = ZoneOffset.ofHours(9)
        val dateBegin = OffsetDateTime.of(2022, 12, 1, 0, 0, 0, 0, offset).toInstant()
        val dateEnd = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, offset).toInstant()
        val entries = HatenaClient.entry.searchEntries(
            SearchType.TAG,
            "test",
            EntriesType.RECENT,
            dateBegin = dateBegin,
            dateEnd = dateEnd
        )
        entries.forEach {
            println("[${formatter.format(it.createdAt.atOffset(offset))}] ${it.title}")
            assert(it.createdAt.isAfter(dateBegin) && it.createdAt.isBefore(dateEnd))
        }
    }

    @Test
    fun searchEntriesWithUsers() = runBlocking {
        for (users in listOf(3, 100, 1000, 10000)) {
            val entries = HatenaClient.entry.searchEntries(
                SearchType.TEXT,
                "hatena",
                EntriesType.RECENT,
                users = users
            )
            entries.forEach { assert(it.count >= users) { "${it.count} / $users" } }
        }
    }

    /*
     * 1) https://b.hatena.ne.jp/entry/s/www.hoge.com/ ==> https://www.hoge.com/
     * 2) https://b.hatena.ne.jp/entry/https://www.hoge.com/ ==> https://www.hoge.com/
     * 3) https://b.hatena.ne.jp/entry/{eid}/comment/{username} ==> https://b.hatena.ne.jp/entry/{eid}  (modifySpecificUrls()を参照)
     * 4) https://b.hatena.ne.jp/entry?url=https~~~
     * 5) https://b.hatena.ne.jp/entry?eid=1234
     * 6) https://b.hatena.ne.jp/entry/{eid}
     * 7) https://b.hatena.ne.jp/entry.touch/s/~~~
     * 8) https://b.hatena.ne.jp/entry/panel/?url=~~~
     *
     * 9) @throws IllegalArgumentException 渡されたurlがエントリURLとして判別不可能
     */
    @Test
    fun getUrlFromEntryUrl_case1() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry/s/www.hoge.com/")
        assertEquals("https://www.hoge.com/", url)
    }

    @Test
    fun getUrlFromEntryUrl_case2() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry/https://www.hoge.com/")
        assertEquals("https://www.hoge.com/", url)
    }

    @Test
    fun getUrlFromEntryUrl_case3() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry/123456789/comment/suihan74")
        assertEquals("https://b.hatena.ne.jp/entry/123456789", url)
    }

    @Test
    fun getUrlFromEntryUrl_case4() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry?url=https://www.hoge.com/")
        assertEquals("https://www.hoge.com/", url)
    }

    @Test
    fun getUrlFromEntryUrl_case4_encoded() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry?url=https%3A%2F%2Fwww.hoge.com%2F")
        assertEquals("https://www.hoge.com/", url)
    }

    @Test
    fun getUrlFromEntryUrl_case5() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry?eid=123456789")
        assertEquals("https://b.hatena.ne.jp/entry/123456789", url)
    }

    @Test
    fun getUrlFromEntryUrl_case6() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry/123456789")
        assertEquals("https://b.hatena.ne.jp/entry/123456789", url)
    }

    @Test
    fun getUrlFromEntryUrl_case7() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry.touch/s/www.hoge.com/")
        assertEquals("https://www.hoge.com/", url)
    }

    @Test
    fun getUrlFromEntryUrl_case8() {
        val url = HatenaClient.entry.getUrl("https://b.hatena.ne.jp/entry/panel/?url=https%3A%2F%2Fwww.hoge.com%2F")
        assertEquals("https://www.hoge.com/", url)
    }

    @Test
    fun getUrlFromEntryUrl_case9_not_matched() {
        runCatching {
            val url = HatenaClient.entry.getUrl("https://localhost/")
        }.onSuccess {
            fail()
        }.onFailure {
            assert(it is IllegalArgumentException)
        }
    }

    @Test
    fun getEntryUrlFromUrl_https() {
        val entryUrl = HatenaClient.entry.getEntryUrl("https://www.hoge.com/")
        assertEquals("https://b.hatena.ne.jp/entry/s/www.hoge.com/", entryUrl)
    }

    @Test
    fun getEntryUrlFromUrl_http() {
        val entryUrl = HatenaClient.entry.getEntryUrl("http://www.hoge.com/")
        assertEquals("https://b.hatena.ne.jp/entry/www.hoge.com/", entryUrl)
    }

    @Test
    fun getEntryUrlFromUrl_invalid_url() {
        runCatching {
            val entryUrl = HatenaClient.entry.getEntryUrl("this is not an url")
        }.onSuccess {
            fail()
        }.onFailure {
            assert(it is IllegalArgumentException)
        }
    }

    @Test
    fun getUserHistoricalEntries() = runBlocking {
        val client = HatenaClient.signIn(rk)
        val entries = client.entry.getUserHistoricalEntries(year = 2020)
        entries.forEach { println(it) }
    }

    @Test
    fun getHatenaHistoricalEntries() = runBlocking {
        val entries = HatenaClient.entry.getHistoricalEntries(year = 2020)
        entries.forEach { println(it) }
    }

    @Test
    fun getHatenaHistoricalEntries_failureCase_over_2020() = runBlocking {
        val result = runCatching {
            val entries = HatenaClient.entry.getHistoricalEntries(year = 2021)
            entries.forEach { println(it) }
        }.onSuccess {
            error("a case where failure is expected has been succeeded.")
        }.onFailure {
            it.printStackTrace()
        }
    }

    @Test
    fun getHatenaHistoricalEntries_failureCase_under_2005() = runBlocking {
        val result = runCatching {
            val entries = HatenaClient.entry.getHistoricalEntries(year = 2004)
            entries.forEach { println(it) }
        }.onSuccess {
            error("a case where failure is expected has been succeeded.")
        }.onFailure {
            it.printStackTrace()
        }
    }

    @Test
    fun getHatenaHistoricalEntries_failureCase_zero() = runBlocking {
        val result = runCatching {
            val entries = HatenaClient.entry.getHistoricalEntries(year = 0)
            entries.forEach { println(it) }
        }.onSuccess {
            error("a case where failure is expected has been succeeded.")
        }.onFailure {
            it.printStackTrace()
        }
    }

    @Test
    fun getHatenaHistoricalEntries_failureCase_under_zero() = runBlocking {
        val result = runCatching {
            val entries = HatenaClient.entry.getHistoricalEntries(year = -1)
            entries.forEach { println(it) }
        }.onSuccess {
            error("a case where failure is expected has been succeeded.")
        }.onFailure {
            it.printStackTrace()
        }
    }

    @Test
    fun getPageTitle() = runBlocking {
        val url = "https://suihan74.github.io/"
        val title = HatenaClient.entry.getPageTitle(url)
        val expectedTitle = "すいはんぶろぐ.io"
        assertEquals(expectedTitle, title)
        println("url: $url, title: $title")
    }

    @Test
    fun getPageTitle_failureCase_unknownHost() = runBlocking {
        val result = runCatching {
            val url = "https://suihan74.github.ioooooooooooooooooooooo/"
            val title = HatenaClient.entry.getPageTitle(url)
            error("url: $url, title: $title")
        }.onFailure {
            it.printStackTrace()  // UnknownHostException
        }
    }

    @Test
    fun getPageTitle_failureCase_unknownPage() = runBlocking {
        val result = runCatching {
            val url = "https://suihan74.github.io/hoge"
            val title = HatenaClient.entry.getPageTitle(url)
            error("url: $url, title: $title")
        }.onFailure {
            it.printStackTrace()  // HttpException
        }
    }

    @Test
    fun getSiteEntries() = runBlocking {
        val entries = HatenaClient.entry.getSiteEntries(
            url = "https://anond.hatelabo.jp/",
            entriesType = EntriesType.HOT
        )
        assert(entries.isNotEmpty()) { "取得できていない" }
        entries.forEach { println(it) }
    }

    @Test
    fun getEntry_from_eid() = runBlocking {
        val eid = 4725889401173682691
        val entry = HatenaClient.entry.getEntry(eid)
        assertEquals(eid, entry.eid)
    }

    @Test
    fun getEntry_from_valid_url() = runBlocking {
        val eid = 4725889401173682691
        val url = "https://anond.hatelabo.jp/20220928094843"
        val entry = HatenaClient.entry.getEntry(url)
        assertEquals(eid, entry.eid)
    }

    @Test
    fun getEntry_from_invalid_url() = runBlocking {
        val url = "https://github.com/suihan74/Satena/blob/main/app/src/main/java/com/suihan74/hatenaLib/HatenaClient.kt"
        val entry = HatenaClient.entry.getEntry(url)
        assertEquals(0L, entry.eid)
    }

    @Test
    fun getRelatedEntries() = runBlocking {
        val url = "https://b.hatena.ne.jp/"
        val relatedEntries = HatenaClient.entry.getRelatedEntries(url)
        assert(relatedEntries.entries.isNotEmpty())
    }

    @Test
    fun getFollowingEntries() = runBlocking {
        val client = HatenaClient.signIn(rk)
        val followingEntries = client.entry.getFollowingEntries()
        followingEntries.forEach { println(it) }
    }

    @Test
    fun getMaintenanceEntries() = runBlocking {
        val entries = HatenaClient.entry.getMaintenanceEntries()
        entries.forEach {
            println(it)
        }
    }

    @Test
    fun getEntry_withMyBookmark() = runBlocking {
        val client = HatenaClient.signIn(rk)
        val entry = client.entry.getEntry(4735775702463931269)
        assertNotNull(entry.bookmarkedData)
    }
}