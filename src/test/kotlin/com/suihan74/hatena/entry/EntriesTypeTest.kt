package com.suihan74.hatena.entry

import org.junit.Assert.assertEquals
import org.junit.Test

class EntriesTypeTest {
    @Test
    fun fromOrdinal() {
        assertEquals(EntriesType.HOT, EntriesType.fromOrdinal(0))
        assertEquals(EntriesType.RECENT, EntriesType.fromOrdinal(1))
    }

    @Test
    fun fromInvalidOrdinal() {
        assertEquals(EntriesType.HOT, EntriesType.fromOrdinal(2))
        assertEquals(EntriesType.HOT, EntriesType.fromOrdinal(Int.MAX_VALUE))
        assertEquals(EntriesType.HOT, EntriesType.fromOrdinal(-1))
        assertEquals(EntriesType.HOT, EntriesType.fromOrdinal(Int.MIN_VALUE))
    }
}