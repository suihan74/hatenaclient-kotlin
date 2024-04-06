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
        SPAM("report.type.spam"),
        /** 誹謗中傷 */
        DEFAMATION("report.type.fud"),
        /** 犯罪予告 */
        CRIME("report.type.crime"),
        /** 差別・侮辱 */
        INSULT("report.type.discrimination"),
        /** その他 */
        OTHERS("report.type.misc")
    }
}
