package com.suihan74.hatena.model.bookmark

import com.suihan74.hatena.model.entry.Entry

data class Report(
    val bookmark: Bookmark,
    val entry: Entry,
    val reason: Reason,
    val comment: String?
) {
    enum class Reason(
        val code: String
    ) {
        /** スパム行為 */
        SPAM("spam"),
        /** 犯罪予告 */
        CRIME("crime_notice"),
        /** 差別、侮辱、嫌がらせ */
        INSULT("insult"),
        /** その他 */
        OTHERS("others")
    }
}
