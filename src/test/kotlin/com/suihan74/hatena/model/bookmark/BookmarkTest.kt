package com.suihan74.hatena.model.bookmark

import com.suihan74.hatena.model.star.StarColor
import com.suihan74.hatena.model.star.StarCount
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class BookmarkTest {
    @Test
    fun lazyProperties() {
        val bookmark = Bookmark(
            Bookmark.User("test", "url"),
            "comment",
            false,
            "link",
            listOf("tag"),
            Instant.MAX,
            listOf(StarCount(StarColor.BLUE, 123))
        )
        assertEquals("test", bookmark.user)
        assertEquals("url", bookmark.userIconUrl)
        assertEquals("comment", bookmark.comment)
        assertEquals(false, bookmark.isPrivate)
        assertEquals("link", bookmark.link)
        assertEquals("tag", bookmark.tags[0])
        assertEquals(Instant.MAX, bookmark.timestamp)
        assertEquals(StarColor.BLUE, bookmark.starCount[0].color)
        assertEquals(123, bookmark.starCount[0].count)

    }
}