package com.suihan74.hatena.star

import com.suihan74.hatena.serializer.StarColorSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * スターカラー
 */
@Serializable(with = StarColorSerializer::class)
enum class StarColor {
    YELLOW,
    RED,
    GREEN,
    BLUE,
    PURPLE,
    ;

    companion object {
        fun fromName(name: String) : StarColor {
            val nameLower = name.toUpperCase(Locale.ENGLISH)
            return values().firstOrNull { it.name == nameLower } ?: YELLOW
        }
    }
}
