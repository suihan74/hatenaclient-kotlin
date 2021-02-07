package com.suihan74.hatena.api

import com.suihan74.hatena.entry.Category
import com.suihan74.hatena.entry.EntriesType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class EntryServiceTest {
    private val client = HatenaClient()

    private suspend fun getEntries(entriesType: EntriesType, category: Category) {
        println("type = " + entriesType.name + " | category = " + category.name)
        client.entry.getEntries(entriesType, category).let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach {
                println(Json.encodeToString(it))
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
}