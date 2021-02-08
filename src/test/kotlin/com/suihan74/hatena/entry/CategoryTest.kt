package com.suihan74.hatena.entry

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryTest {
    @Test
    fun fromId() {
        assertEquals(Category.ALL, Category.fromId(0))
        assertEquals(Category.KNOWLEDGE, Category.fromId(5))
        assertEquals(Category.FUN, Category.fromId(9))
    }

    @Test
    fun fromInvalidId() {
        assertEquals(Category.ALL, Category.fromId(10))
        assertEquals(Category.ALL, Category.fromId(Int.MIN_VALUE))
        assertEquals(Category.ALL, Category.fromId(Int.MAX_VALUE))
    }
}