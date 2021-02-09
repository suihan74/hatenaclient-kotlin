package com.suihan74.hatena.api

import com.suihan74.hatena.entry.*
import com.suihan74.hatena.exception.InvalidResponseException
import com.suihan74.hatena.extension.int
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.reflect.Type

/**
 * エントリ関連のAPI
 */
interface EntryService {
    /**
     * カテゴリを指定して人気/新着エントリを取得する
     *
     * @param entriesType 人気or新着
     * @param category カテゴリ
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @param includeAMPUrls AMP用のURLを含める
     * @param includeBookmarkedData 自分のブクマ情報を含める(サインイン済みの場合)
     * @param includeBookmarksByVisitor
     * @param includeBookmarksOfFollowings フォローしているユーザーのブクマ情報を含める(サインイン済みの場合)
     * @param includeAds はてなから提供される広告を含める
     */
    @GET("api/ipad.{type}.json")
    suspend fun getEntries(
        @Path("type") @EntriesTypeQuery(EntriesTypeUsage.ENTRIES) entriesType: EntriesType,
        @Query("category_id") category: Category,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_amp_urls") includeAMPUrls: Boolean = true,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true,
        @Query("include_bookmarks_of_followings") includeBookmarksOfFollowings: Boolean = true,
        @Query("ad") includeAds: Boolean = false
    ) : List<EntryItem>

    // ------ //

    /**
     * Issueを指定して人気/新着エントリを取得する
     *
     * @param entriesType 人気or新着
     * @param issue 特集
     * @param limit 最大件数
     * @param offset 取得開始位置
     * @param includeAMPUrls AMP用のURLを含める
     * @param includeBookmarkedData 自分のブクマ情報を含める(サインイン済みの場合)
     * @param includeBookmarksByVisitor
     * @param includeBookmarksOfFollowings フォローしているユーザーのブクマ情報を含める(サインイン済みの場合)
     * @param includeAds はてなから提供される広告を含める
     */
    @GET("api/internal/cambridge/issue/{issue_id}/{type}")
    suspend fun getIssueEntries(
        @Path("type") @EntriesTypeQuery(EntriesTypeUsage.ISSUE_ENTRIES) entriesType: EntriesType,
        @Path("issue_id") issue: Issue,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_amp_urls") includeAMPUrls: Boolean = true,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true,
        @Query("include_bookmarks_by_visitor") includeBookmarksByVisitor: Boolean = true,
        @Query("include_bookmarks_of_followings") includeBookmarksOfFollowings: Boolean = true,
        @Query("ad") includeAds: Boolean = false
    ) : IssueEntriesResponse

    /**
     * 指定カテゴリの特集一覧を取得する
     */
    @GET("api/internal/cambridge/category/{category_id}/issues")
    suspend fun getIssues(@Path("category_id") category: Category) : IssuesResponse

    // ------ //

    /**
     * エントリをクエリ検索する
     */
    @GET("api/ipad.search/{searchType}")
    suspend fun searchEntries(
        @Path("searchType") searchType: SearchType,
        @Query("q", encoded = true) query: String,
        @Query("sort") @EntriesTypeQuery(EntriesTypeUsage.SEARCH_SORT) sortType: EntriesType = EntriesType.RECENT,
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null,
        @Query("include_bookmarked_data") includeBookmarkedData: Boolean = true
    ) : List<EntryItem>
}

// ------ //

/**
 * 指定ページのエントリIDを取得する
 *
 * @throws retrofit2.HttpException code=404: ブクマが一件も登録されていない
 * @throws retrofit2.HttpException 通信失敗
 * @throws InvalidResponseException レスポンスの処理に失敗
 */
@Suppress("BlockingMethodInNonBlockingContext")
suspend fun EntryService.getEntryId(url: String) : Long {
    val entryUrl = HatenaClient.getEntryUrl(url)
    return HatenaClient.generalService.getHtml(entryUrl) { html ->
        html.getElementsByTag("html")!!
            .first()
            .attr("data-entry-eid")
            .toLong()
    }
}

// ------ //

/**
 * 認証が必要なエントリ関係API
 */
interface CertifiedEntryService : EntryService {
    /**
     * ユーザーがブクマしたエントリ一覧を取得する
     *
     * @param limit 最大件数
     * @param offset 取得開始位置
     */
    @GET("api/ipad.mybookmarks")
    suspend fun getBookmarkedEntries(
        @Query("limit") limit: Int? = null,
        @Query("of") offset: Int? = null
    ) : List<EntryItem>
}


// ------ //

/**
 * (主に)クエリパラメータを文字列に変換するためのコンバータ
 */
internal object EntryConverterFactory : Converter.Factory() {
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
            else -> null
        }

    private fun selectEntriesTypeConverter(annotations: Array<Annotation>) : Converter<EntriesType, String>? {
        val type = annotations.firstOrNull { it is EntriesTypeQuery } as? EntriesTypeQuery
        return when (type?.value) {
            EntriesTypeUsage.ISSUE_ENTRIES -> EntriesTypeForIssueConverter
            EntriesTypeUsage.SEARCH_SORT -> EntriesTypeForSearchConverter
            else -> EntriesTypeConverter
        }
    }

    // ------ //

    internal object CategoryConverter : Converter<Category, String> {
        override fun convert(value: Category) = value.code
    }

    internal object IssueConverter : Converter<Issue, String> {
        override fun convert(value: Issue) = value.code
    }

    internal object SearchTypeConverter : Converter<SearchType, String> {
        override fun convert(value: SearchType) = value.code
    }

    internal object EntriesTypeConverter : Converter<EntriesType, String> {
        override fun convert(value: EntriesType) = value.code
    }

    internal object EntriesTypeForIssueConverter : Converter<EntriesType, String> {
        override fun convert(value: EntriesType) = value.codeForIssues
    }

    internal object EntriesTypeForSearchConverter : Converter<EntriesType, String> {
        override fun convert(value: EntriesType) = value.codeForSearch
    }

    internal object BooleanConverter : Converter<Boolean, String> {
        override fun convert(value: Boolean) = value.int.toString()
    }
}