package com.suihan74.hatena.api

import com.suihan74.hatena.entry.*
import com.suihan74.hatena.extension.int
import com.suihan74.hatena.star.StarColor
import com.suihan74.hatena.star.StarPalette
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * パラメータにオブジェクトを使用するためのコンバータ
 */
internal object ObjectParameterConverterFactory : Converter.Factory() {
    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? =
        when (type) {
            Category::class.java -> CategoryConverter
            SearchType::class.java -> SearchTypeConverter
            EntriesType::class.java -> selectEntriesTypeConverter(annotations)
            Issue::class.java -> IssueConverter
            Boolean::class.java -> BooleanConverter
            StarColor::class.java -> StarColorConverter
            StarPalette::class.java -> StarPaletteConverter
            Instant::class.java -> InstantConverter
            else -> null
        }

    private fun selectEntriesTypeConverter(annotations: Array<Annotation>) : Converter<EntriesType, String> {
        val type = annotations.firstOrNull { it is EntriesTypeQuery } as? EntriesTypeQuery
        return when (type?.value) {
            EntriesTypeUsage.ISSUE_ENTRIES -> EntriesTypeForIssueConverter
            EntriesTypeUsage.SEARCH_SORT -> EntriesTypeForSearchConverter
            else -> EntriesTypeConverter
        }
    }

    // ------ //

    object CategoryConverter : Converter<Category, String> {
        override fun convert(value: Category) = value.code
    }

    object IssueConverter : Converter<Issue, String> {
        override fun convert(value: Issue) = value.code
    }

    object SearchTypeConverter : Converter<SearchType, String> {
        override fun convert(value: SearchType) = value.code
    }

    object EntriesTypeConverter : Converter<EntriesType, String> {
        override fun convert(value: EntriesType) = value.code
    }

    object EntriesTypeForIssueConverter : Converter<EntriesType, String> {
        override fun convert(value: EntriesType) = value.codeForIssues
    }

    object EntriesTypeForSearchConverter : Converter<EntriesType, String> {
        override fun convert(value: EntriesType) = value.codeForSearch
    }

    object BooleanConverter : Converter<Boolean, String> {
        override fun convert(value: Boolean) = value.int.toString()
    }

    object StarColorConverter : Converter<StarColor, String> {
        override fun convert(value: StarColor) = value.name.lowercase()
    }

    object StarPaletteConverter : Converter<StarPalette, String> {
        override fun convert(value: StarPalette) = value.token
    }

    object InstantConverter : Converter<Instant, String> {
        private val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
        private val offset = ZoneOffset.ofHours(9)
        override fun convert(value: Instant) : String = value.atOffset(offset).format(formatter)
    }
}