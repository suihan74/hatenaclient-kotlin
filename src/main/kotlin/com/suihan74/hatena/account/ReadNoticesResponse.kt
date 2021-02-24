package com.suihan74.hatena.account

import com.suihan74.hatena.serializer.EpochTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ReadNoticesResponse(
    /**
     * 最後に通知確認した時刻
     */
    @SerialName("last_seen")
    @Serializable(with = EpochTimeSerializer::class)
    val lastSeen : Instant,

    /**
     * 成功時"ok"
     *
     * TODO: 失敗時何が入るか
     */
    val status : String
)
