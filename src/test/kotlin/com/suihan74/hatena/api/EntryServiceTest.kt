package com.suihan74.hatena.api

import com.suihan74.hatena.entry.Category
import com.suihan74.hatena.entry.EntriesType
import com.suihan74.hatena.entry.Issue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException

class EntryServiceTest : AccountServiceTestCredentials() {
    private suspend fun getEntries(entriesType: EntriesType, category: Category) {
        println("type = " + entriesType.name + " | category = " + category.name)
        HatenaClient.entry.getEntries(entriesType, category).let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach {
                println(Json.encodeToString(it))
                println("  entryUrl = " + it.entryUrl)
                println("  rootUrl = " + it.rootUrl)
                println("  imageUrl = " + it.imageUrl)
                println("  faviconUrl = " + it.faviconUrl)
            }
        }
    }

    private suspend fun getEntries(entriesType: EntriesType, issue: Issue) {
        println("type = " + entriesType.name + " | category = " + issue.name)
        HatenaClient.entry.getEntries(entriesType, issue).let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach {
                println(Json.encodeToString(it))
                println("  entryUrl = " + it.entryUrl)
                println("  rootUrl = " + it.rootUrl)
                println("  imageUrl = " + it.imageUrl)
                println("  faviconUrl = " + it.faviconUrl)
            }
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
            entries.forEach {
                println(Json.encodeToString(it))
                println("  entryUrl = " + it.entryUrl)
                println("  rootUrl = " + it.rootUrl)
                println("  imageUrl = " + it.imageUrl)
                println("  faviconUrl = " + it.faviconUrl)
            }
        }
    }

    @Test
    fun allIssues() = runBlocking {
        Category.values().forEach {
            println("===== " + it.name + " =====")
            runCatching {
                HatenaClient.entry.getIssues(it).forEachIndexed { index, issue ->
                    println(Json.encodeToString(issue))
                    issue.entry!!.let { entry ->
                        println("  entryUrl = " + entry.entryUrl)
                        println("  rootUrl = " + entry.rootUrl)
                        println("  imageUrl = " + entry.imageUrl)
                        println("  faviconUrl = " + entry.faviconUrl)
                    }

                    if (index == 0) {
                        getEntries(EntriesType.HOT, issue)
                        getEntries(EntriesType.RECENT, issue)
                    }
                }
            }.onFailure { e ->
                // `ALL`, `GENERAL`ではIssueが得られない
                e.printStackTrace()
                assert(
                    (it == Category.ALL || it == Category.GENERAL) &&
                        e is HttpException && e.code() == 400
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
}