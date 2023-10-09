package com.suihan74.hatena.model.star

import com.suihan74.hatena.serializer.StarColorSerializer
import kotlinx.serialization.Serializable

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
            val nameUpper = name.uppercase()
            return values().firstOrNull { it.name == nameUpper } ?: YELLOW
        }
    }
}
