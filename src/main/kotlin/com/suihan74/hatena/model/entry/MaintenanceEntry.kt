package com.suihan74.hatena.model.entry

import com.suihan74.hatena.serializer.InstantISO8601Serializer
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * メンテナンス情報
 */
@Serializable
data class MaintenanceEntry(
    val id: String,

    val title: String,

    val body: String,

    val resolved: Boolean,

    val url: String,

    @Serializable(with = InstantISO8601Serializer::class)
    val createdAt: Instant,

    @Serializable(with = InstantISO8601Serializer::class)
    val updatedAt: Instant
)