package com.suihan74.hatena.api

import com.suihan74.hatena.entry.Category
import com.suihan74.hatena.entry.EntriesType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class EntryServiceTest {
    @Test
    fun getHotEntries() = runBlocking {
        HatenaClient().entry.getEntries(EntriesType.HOT, Category.All).let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach {
                println(Json.encodeToString(it))
            }
        }
    }

    @Test
    fun getRecentEntries() = runBlocking {
        HatenaClient().entry.getEntries(EntriesType.RECENT, Category.All).let { entries ->
            assert(entries.isNotEmpty())
            entries.forEach {
                println(Json.encodeToString(it))
            }
        }
    }
}