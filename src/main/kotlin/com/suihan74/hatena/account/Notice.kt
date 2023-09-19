package com.suihan74.hatena.account

import com.suihan74.hatena.serializer.EpochTimeSerializer
import com.suihan74.hatena.serializer.StarColorSerializer
import com.suihan74.hatena.star.StarColor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@kotlinx.serialization.Serializable
data class Notice(
    @Serializable(with = EpochTimeSerializer::class)
    val created : Instant,

    @Serializable(with = EpochTimeSerializer::class)
    val modified : Instant,

    @SerialName("object")
    val objects : List<NoticeObject>,

    val verb : String,  // "star" など

    @SerialName("subject")
    val link : String,

    @SerialName("user_name")
    val user : String,

    val metadata : NoticeMetadata? = null
) {
    val eid : Long by lazy {
        if (verb == Verb.STAR.value) {
            val idx = link.lastIndexOf('-') + 1
            link.substring(idx).toLong()
        }
        else {
            throw IllegalArgumentException("notice's verb: $verb")
        }
    }

    // ------ //

    enum class Verb(val value : String) {
        ADD_FAVORITE("add_favorite"),
        BOOKMARK("bookmark"),
        STAR("star")
    }

    // ------ //

    @Serializable
    data class NoticeObject(
        val user : String,

        @Serializable(with = StarColorSerializer::class)
        val color : StarColor = StarColor.YELLOW
    )

    // ------ //

    @Serializable
    data class NoticeMetadata(
        @SerialName("subject_title")
        val subjectTitle : String
    )
}
