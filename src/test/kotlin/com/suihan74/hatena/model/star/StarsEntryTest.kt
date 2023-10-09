package com.suihan74.hatena.model.star

import com.suihan74.hatena.model.star.ColorStars
import com.suihan74.hatena.model.star.Star
import com.suihan74.hatena.model.star.StarColor
import com.suihan74.hatena.model.star.StarsEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class StarsEntryTest {
    private val mockStarsEntry = StarsEntry(
        url = "https://localhost/",
        stars = listOf(
            Star(color = StarColor.YELLOW, user = "user0"),
            Star(color = StarColor.YELLOW, user = "user1"),
            Star(color = StarColor.YELLOW, user = "user2", count = 3),
        ),
        coloredStars = listOf(
            ColorStars(color = StarColor.GREEN, listOf(Star(user = "user0"), Star(user = "user3")))
        )
    )

    private val mockEmptyStarsEntry = StarsEntry(
        url = "https://localhost/",
        stars = emptyList()
    )

    @Test
    fun starsCount() {
        val yellowStarsCount = mockStarsEntry.starsCount(StarColor.YELLOW)
        assertEquals(5, yellowStarsCount)

        val greenStarsCount = mockStarsEntry.starsCount(StarColor.GREEN)
        assertEquals(2, greenStarsCount)

        // 存在しない色 == 0
        val purpleStarsCount = mockStarsEntry.starsCount(StarColor.PURPLE)
        assertEquals(0, purpleStarsCount)
    }

    @Test
    fun emptyStarsCount() {
        val emptyYellowStarsCount = mockEmptyStarsEntry.starsCount(StarColor.YELLOW)
        assertEquals(0, emptyYellowStarsCount)

        val emptyGreenStarsCount = mockEmptyStarsEntry.starsCount(StarColor.GREEN)
        assertEquals(0, emptyGreenStarsCount)
    }

    @Test
    fun totalCount() {
        assertEquals(7, mockStarsEntry.totalCount)
    }

    @Test
    fun emptyTotalCount() {
        assertEquals(0, mockEmptyStarsEntry.totalCount)
    }

    @Test
    fun allStars() {
        val allStars = mockStarsEntry.allStars
        allStars.forEach {
            println(Json.encodeToString(it))
        }
    }

    @Test
    fun emptyAllStars() {
        val emptyAllStars = mockEmptyStarsEntry.allStars
        assertEquals(0, emptyAllStars.size)
        emptyAllStars.forEach {
            println(Json.encodeToString(it))
        }
    }
}