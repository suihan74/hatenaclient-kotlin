package com.suihan74.hatena.api

import com.suihan74.hatena.entry.Category
import com.suihan74.hatena.entry.EntriesType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                HatenaClient.entry.getIssues(it).forEach { issue ->
                    println(Json.encodeToString(issue))
                    issue.entry!!.let { entry ->
                        println("  entryUrl = " + entry.entryUrl)
                        println("  rootUrl = " + entry.rootUrl)
                        println("  imageUrl = " + entry.imageUrl)
                        println("  faviconUrl = " + entry.faviconUrl)
                    }
                }
            }.onFailure { e ->
                e.printStackTrace()
                assert(
                    (it == Category.ALL || it == Category.GENERAL) &&
                        e is HttpException && e.code() == 400
                )
            }
        }
    }
}