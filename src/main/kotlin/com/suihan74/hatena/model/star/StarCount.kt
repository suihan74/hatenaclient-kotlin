package com.suihan74.hatena.model.star

import kotlinx.serialization.Serializable

/**
 * ブクマに含まれるスター数の情報
 */
@Serializable
data class StarCount (
    val color : StarColor,
    val count : Int
)
