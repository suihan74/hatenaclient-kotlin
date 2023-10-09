package com.suihan74.hatena.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.suihan74.hatena.HatenaClient
import com.suihan74.hatena.HatenaClientBase
import com.suihan74.hatena.HatenaClientBaseNoCertificationRequired
import com.suihan74.hatena.api.BookmarkAPI
import com.suihan74.hatena.api.ObjectParameterConverterFactory
import com.suihan74.hatena.model.bookmark.TweetsAndClicks
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import java.net.URLEncoder

internal class BookmarkServiceTest : AccountServiceTestCredentials() {
    private val testUrl = "https://togetter.com/li/1647315"

    private fun createMock(responseBuilder: MockWebServer.()->Unit) : Pair<MockWebServer, HatenaClientBase> {
        val server = MockWebServer().also {
            responseBuilder(it)
        }

        val hatenaApi = spy<HatenaClientBaseNoCertificationRequired>().apply {
            whenever(baseUrlW) doReturn server.url("").toString()
            whenever(baseUrlB) doReturn server.url("").toString()
            whenever(baseUrlS) doReturn server.url("").toString()
        }

        return server to hatenaApi
    }

    @Test
    fun getRecentBookmarks() = runBlocking {
        val (server, hatenaApi) = createMock {
            enqueue(MockResponse().setResponseCode(403))
            enqueue(MockResponse().setResponseCode(404))
            enqueue(
                MockResponse().setBody("""
                {"cursor":"### test_response ###","bookmarks":[{"is_private":false,"link":"","tags":[],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/nikuyoshi/profile.png","name":"nikuyoshi"},"star_count":[],"comment":"","timestamp":"2021-01-07T09:28:21+09:00"},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/mohno/profile.png","name":"mohno"},"timestamp":"2021-01-07T00:58:15+09:00","comment":"\u30db\u30f3\u30c8\u3060\u3002\u306f\u3066\u30d6\u306e\u30bf\u30a4\u30c8\u30eb\u304c\u5909\u66f4\u3067\u304d\u306a\u3044\u3002\u3069\u3093\u306a\u30de\u30b8\u30c3\u30af\uff1f\u5143\u30bf\u30a4\u30c8\u30eb\u2192\u300c\u300c\u3053\u306e\u5e74\u672b\u5e74\u59cb\u306b\u30b3\u30fc\u30c9\u5168\u304f\u66f8\u3044\u3066\u306a\u3044\u4eba\u306f\u30a8\u30f3\u30b8\u30cb\u30a2\u5411\u3044\u3066\u306a\u3044\u300dZOZO\u30c6\u30af\u30ce\u30ed\u30b8\u30fc\u30baCTO\u306e\u30c4\u30a4\u30fc\u30c8\u306b\u30ac\u30af\u30d6\u30eb\u3059\u308b\u4eba\u3005 - Togetter\u300d","star_count":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/mohno","tags":["togetter","\u306f\u3066\u306a","\u306f\u3066\u306a\u30d6\u30c3\u30af\u30de\u30fc\u30af","\u4eca\u6751\u96c5\u5e78","ZOZO","CTO","\u958b\u767a","\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0","twitter","\u767a\u8a00"],"is_private":false},{"user":{"name":"matsuza","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/matsuza/profile.png"},"comment":"\u30d0\u30eb\u30b9\u3063\u3066\u4e00\u7dd2\u306b\u8a00\u3046\u306e\u697d\u3057\u3044\u3051\u3069\u3001\u4e00\u7dd2\u306b\u30d0\u30eb\u30b9\u3068\u8a00\u3048\u3063\u3066\u8a00\u308f\u308c\u305f\u3089\u697d\u3057\u304f\u306a\u3044","timestamp":"2021-01-06T17:22:00+09:00","star_count":[],"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/matsuza","tags":[]},{"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/kanazawawan","tags":[],"timestamp":"2021-01-06T14:54:47+09:00","comment":"\u30a8\u30f3\u30b8\u30cb\u30a2\u306e\u7ba1\u7406\u8005\u5411\u3044\u3066\u306a\u3044\u4eba\u3060\u306a\u3002\u30a8\u30f3\u30b8\u30cb\u30a2\u306e\u4e2d\u306b\u306f\u30bb\u30f3\u30b9\u826f\u304f\u3066\u4e00\u4eba\u3067\u4f55\u3067\u3082\u3067\u304d\u308b\u3051\u3069\u3001\u4ed6\u4eba\u304c\u30d7\u30e9\u30a4\u30d9\u30fc\u30c8\u306b\u8e0f\u307f\u8fbc\u3093\u3067\u304d\u305f\u308a\u7406\u4e0d\u5c3d\u306a\u6271\u3044\u3046\u3051\u308b\u3068\u3001\u3059\u3050\u8f9e\u3081\u3066\u3044\u304f\u30a8\u30f3\u30b8\u30cb\u30a2\u591a\u3044\u3088\u3002\u3053\u3093\u306a\u767a\u8a00\u3042\u308a\u3048\u306a\u3044","star_count":[],"user":{"name":"kanazawawan","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/kanazawawan/profile.png"}},{"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/strawberryhunter","tags":["\u672a\u5206\u985e"],"user":{"name":"strawberryhunter","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/strawberryhunter/profile.png"},"star_count":[],"comment":"\u300c\u5e74\u672b\u5e74\u59cb\u306b\u30b3\u30fc\u30c9\u5168\u304f\u66f8\u3044\u3066\u306a\u3044\u4eba\u306f\u30a8\u30f3\u30b8\u30cb\u30a2\u5411\u3044\u3066\u306a\u3044\u3093\u3058\u3083\u306a\u3044\u3067\u3059\u304b\u306d\u300d\u306a\u305c\u708e\u4e0a\u6c17\u5473\u306a\u306e\u304b\u3002\u3053\u306e\u4eba\u304c\u305d\u3046\u601d\u3046\u3060\u3051\u3067\u5f37\u8981\u3055\u308c\u3066\u3044\u308b\u308f\u3051\u3067\u306f\u306a\u3044\u3002\u305d\u308c\u304b\u5e38\u8b58\u5316\u3059\u308b\u3068\u540c\u8abf\u5727\u529b\u3067\u30b3\u30fc\u30c9\u66f8\u304f\u308f\u3051\uff1f\u6ed1\u7a3d\u3060\u308f\u3002","timestamp":"2021-01-05T22:13:14+09:00"},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/ryunosinfx/profile.png","name":"ryunosinfx"},"timestamp":"2021-01-05T20:28:18+09:00","comment":"\u7d75\u304b\u304d\u304c\u606f\u629c\u304d\u306b\u7d75\u3092\u63cf\u304f\u30ec\u30d9\u30eb\u3058\u3083\u306a\u3044\u3068\u52d9\u307e\u3089\u306a\u3044\u306f\u6b63\u3060\u3068\u601d\u3046\u304c\u3001SIer\u306f\u8a08\u7b97\u6a5f\u3084\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0\u304c\u597d\u304d\u3067\u3082\u306a\u3044\u4eba\u9593\u3092\u6226\u529b\u5316\u3057\u3066\u7a3c\u3050\u30b7\u30b9\u30c6\u30e0\u904b\u7528\u3001\u8ab0\u304b\u306b\u4f5c\u696d\u30ec\u30fc\u30f3\u3092\u7528\u610f\u3057\u3066\u3082\u3089\u3063\u3066\u3058\u3083\u4f5c\u308c\u308b\u30d6\u30c4\u306e\u6c34\u6e96\u304c\u306d","star_count":[],"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/ryunosinfx","tags":[]},{"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/cocoasynn","is_private":false,"tags":[],"user":{"name":"cocoasynn","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/cocoasynn/profile.png"},"star_count":[],"comment":"\u4e00\u5ea6\u4e16\u306e\u4e2d\u306b\u51fa\u3066\u3057\u307e\u3063\u305f\u3082\u306e\u306f\u6d88\u3048\u306a\u3044\u5b9a\u671f","timestamp":"2021-01-05T18:13:37+09:00"},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/kw5/profile.png","name":"kw5"},"star_count":[],"comment":"","timestamp":"2021-01-05T17:57:45+09:00","link":"","tags":[],"is_private":false},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/table/profile.png","name":"table"},"comment":"\u3068\u3045\u304e\u3083\u3063\u305f\u3093\u30d6\u30b3\u30e1\u3057\u3066\u308b\u304f\u3089\u3044\u30bb\u30fc\u30d5\u306a\u5185\u5bb9\u3060\u3068\u601d\u3063\u3066\u305f\u3093\u3060\u304c\u3001\u5229\u7528\u898f\u7d04\u306e\u3069\u3053\u306b\u89e6\u308c\u305f\u3093\u3060\u3088","timestamp":"2021-01-05T17:23:16+09:00","star_count":[{"count":5,"color":"normal"}],"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/table","is_private":false},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/k92aki1004/profile.png","name":"k92aki1004"},"comment":"","timestamp":"2021-01-05T17:22:14+09:00","star_count":[],"is_private":false,"link":"","tags":[]},{"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/lenhai","tags":["Togetter","\u306f\u3066\u30d6"],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/lenhai/profile.png","name":"lenhai"},"timestamp":"2021-01-05T17:10:17+09:00","comment":"\u9b5a\u62d3\u2192https://archive.is/bIEZQ \uff0f\u4ee5\u524d\u306fTogetter\u5074\u304c\u5229\u7528\u898f\u7d04\u306b\u57fa\u3065\u304d\u975e\u516c\u958b\u3067404 Not Found\u3067\u3082\u3001\u306f\u3066\u30d6\u5074\u3067\u306f\u6d88\u3048\u305f\u30bf\u30a4\u30c8\u30eb\u3092\u518d\u7de8\u96c6\u3067\u304d\u305f\u3051\u3069\u3001\u4eca\u306f\u30a8\u30f3\u30c8\u30ea\u30fc\u306e\u7de8\u96c6\u304c\u8868\u793a\u3067\u304d\u305a\u3001\u5f37\u5f15\u306b\u3084\u3063\u3066\u3082405 Method Not Allowed\u307f\u305f\u3044\uff1f","star_count":[{"color":"normal","count":2}]},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/spark7/profile.png","name":"spark7"},"comment":"\u6f2b\u753b\u306e\u606f\u629c\u304d\u306b\u6f2b\u753b\u63cf\u304f\u3088\u3046\u306a\u6027\u683c\u3058\u3083\u306a\u3044\u3068\u5546\u696d\u306b\u5411\u3044\u3066\u306a\u3044\u7684\u306a\u3084\u3064\u3067\u3057\u3087\u3002\u30d5\u30ea\u30fc\u3068\u96c7\u308f\u308c\u3068\u3001\u30a4\u30e1\u30fc\u30b8\u3057\u3066\u308b\u30a8\u30f3\u30b8\u30cb\u30a2\u50cf\u306e\u9f5f\u9f6c\u304c\u71c3\u3048\u305f\u539f\u56e0\u304b\u3002 https://archive.is/bIEZQ","timestamp":"2021-01-05T17:05:50+09:00","star_count":[{"color":"normal","count":2}],"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/spark7","tags":[]},{"comment":"https://web.archive.org/web/20210104223629/https://togetter.com/li/1647315","timestamp":"2021-01-05T16:44:09+09:00","star_count":[{"color":"green","count":1},{"count":3,"color":"normal"}],"user":{"name":"internetkun","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/internetkun/profile.png"},"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/internetkun","is_private":false},{"timestamp":"2021-01-05T16:35:02+09:00","comment":"\u201c\u3053\u306e\u307e\u3068\u3081\u306f\u3001\u5229\u7528\u898f\u7d04\u306b\u57fa\u3065\u304d\u975e\u516c\u958b\u3068\u306a\u3063\u3066\u304a\u308a\u307e\u3059\u3002\u3054\u4e86\u627f\u304f\u3060\u3055\u3044\u3002\u201d","star_count":[{"color":"normal","count":3}],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/prozorec/profile.png","name":"prozorec"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/prozorec","is_private":false,"tags":[]},{"star_count":[{"count":1,"color":"normal"}],"comment":"\u30a4\u30ad\u30ea\u6563\u3089\u304b\u3057\u3066\u308b\u306a\u3041\u3002","timestamp":"2021-01-05T16:16:19+09:00","user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/astefalcon/profile.png","name":"astefalcon"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/astefalcon","tags":[],"is_private":false},{"link":"","tags":[],"is_private":false,"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/jz5_public/profile.png","name":"jz5_public"},"timestamp":"2021-01-05T16:08:18+09:00","comment":"","star_count":[]},{"comment":"","timestamp":"2021-01-05T15:47:43+09:00","star_count":[],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/anguilla/profile.png","name":"anguilla"},"link":"","tags":["togetter","\u4ed5\u4e8b"],"is_private":false},{"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/vaaaaaanquish","is_private":false,"user":{"name":"vaaaaaanquish","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/vaaaaaanquish/profile.png"},"star_count":[{"count":1,"color":"normal"}],"comment":"not found","timestamp":"2021-01-05T15:37:20+09:00"},{"comment":"\"\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0\u30b9\u30af\u30fc\u30eb\u901a\u3063\u3066\u308b\u304b\u3069\u3046\u304b\u3068\u304b\u3069\u3046\u3067\u3082\u3088\u304f\u3066\u3001\u3053\u306e\u5e74\u672b\u5e74\u59cb\u306b\u30b3\u30fc\u30c9\u5168\u304f\u66f8\u3044\u3066\u306a\u3044\u4eba\u306f\u30a8\u30f3\u30b8\u30cb\u30a2\u5411\u3044\u3066\u306a\u3044\u3093\u3058\u3083\u306a\u3044\u3067\u3059\u304b\u306d\u3001\u305d\u308c\u3050\u3089\u3044\u597d\u5947\u5fc3\u304c\u5fc5\u8981\u306a\u8077\u696d\u3060\u3068\u304a\u3082\u3046\u3051\u3069\"","timestamp":"2021-01-05T15:35:31+09:00","star_count":[],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/yuuAn/profile.png","name":"yuuAn"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/yuuAn","tags":[],"is_private":false},{"user":{"name":"viperbjpn","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/viperbjpn/profile.png"},"star_count":[{"count":1,"color":"normal"}],"comment":"Not found....","timestamp":"2021-01-05T15:26:02+09:00","link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/viperbjpn","is_private":false,"tags":[]},{"link":"","is_private":false,"tags":["\u3042\u3068\u3067\u8aad\u3080"],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/greenmold/profile.png","name":"greenmold"},"timestamp":"2021-01-05T15:21:32+09:00","comment":"","star_count":[]},{"is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/mas-higa","tags":[],"timestamp":"2021-01-05T14:58:14+09:00","comment":"\u4f55\u306e\u30a8\u30f3\u30b8\u30cb\u30a2\u306e\u8a71\u3060\u3088\u3002\u516c\u306a\u767a\u8a00\u3067\u8a00\u8449\u306e\u4f7f\u3044\u65b9\u304c\u3044\u3044\u52a0\u6e1b\u306a\u306e CTO \u306b\u5411\u3044\u3066\u306a\u3044\u306e\u3067\u306f?","star_count":[{"color":"normal","count":2}],"user":{"name":"mas-higa","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/mas-higa/profile.png"}},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/koyareamo/profile.png","name":"koyareamo"},"star_count":[],"timestamp":"2021-01-05T14:12:25+09:00","comment":"","tags":["\u3042\u3068\u3067\u8aad\u3080"],"link":"","is_private":false},{"tags":["\u30a8\u30f3\u30b8\u30cb\u30a2","\u4ed5\u4e8b","togetter","\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0","programming","\u30d7\u30ed\u30b0\u30e9\u30de","\u8003\u3048\u65b9"],"link":"","is_private":false,"user":{"name":"moritata","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/moritata/profile.png"},"comment":"","timestamp":"2021-01-05T14:11:24+09:00","star_count":[]},{"link":"","tags":[],"is_private":false,"comment":"","timestamp":"2021-01-05T14:04:35+09:00","star_count":[],"user":{"name":"cozima0210","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/cozima0210/profile.png"}},{"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/butujo","is_private":false,"user":{"name":"butujo","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/butujo/profile.png"},"star_count":[],"timestamp":"2021-01-05T13:45:20+09:00","comment":"\u9b5a\u62d3\u306a\u3044\u3093\u3060\uff08 ;  ; \uff09"},{"comment":"\u3053\u306e\u5e74\u672b\u5e74\u59cb\u306b\u8d77\u696d\u30a2\u30a4\u30c7\u30a3\u30a2\u4e00\u3064\u51fa\u3055\u306a\u3044\u5974\u306f\u7d4c\u55b6\u8005\u306b\u5411\u3044\u3066\u3044\u306a\u3044\u306a","timestamp":"2021-01-05T13:43:09+09:00","star_count":[{"count":1,"color":"normal"}],"user":{"name":"omaenankakurashicategoryda","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/omaenankakurashicategoryda/profile.png"},"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/omaenankakurashicategoryda","is_private":false},{"link":"","tags":["twitter","\u4ed5\u4e8b","\u8003\u3048\u65b9"],"is_private":false,"star_count":[],"timestamp":"2021-01-05T13:38:22+09:00","comment":"","user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/kisiritooru/profile.png","name":"kisiritooru"}},{"user":{"name":"hiby","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/hiby/profile.png"},"star_count":[{"color":"normal","count":6}],"timestamp":"2021-01-05T13:36:25+09:00","comment":"\uff1e\u3053\u306e\u307e\u3068\u3081\u306f\u3001\u5229\u7528\u898f\u7d04\u306b\u57fa\u3065\u304d\u975e\u516c\u958b\u3068\u306a\u3063\u3066\u304a\u308a\u307e\u3059\u3002\u3054\u4e86\u627f\u304f\u3060\u3055\u3044\u3002\u3000\u30bf\u30a4\u30c8\u30eb\u307e\u3067\u6d88\u3055\u308c\u305f\u3002","link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/hiby","tags":[],"is_private":false},{"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/renos","is_private":false,"user":{"name":"renos","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/renos/profile.png"},"star_count":[{"count":12,"color":"normal"}],"timestamp":"2021-01-05T13:12:18+09:00","comment":"\u3042\u308c\u3001\u6d88\u3048\u3066\u3082\u306f\u3066\u30d6\u5074\u306f\u30bf\u30a4\u30c8\u30eb\u6b8b\u3063\u3066\u305f\u6c17\u304c\u3059\u308b\u3093\u3060\u3051\u3069\u306a\u3093\u304b\u5909\u308f\u3063\u305f\u306e\u304b\u2026\uff1f"},{"link":"","is_private":false,"tags":[],"user":{"name":"sushidoggo","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/sushidoggo/profile.png"},"timestamp":"2021-01-05T13:11:51+09:00","comment":"","star_count":[]},{"comment":"\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0\u30b9\u30af\u30fc\u30eb\u3067\u6559\u3048\u3066\u3082\u3089\u3063\u305f\u3053\u3068\u3057\u304b\u3084\u3089\u305a\u3001\u305d\u308c\u3067\u6e80\u8db3\u3057\u3066\u81ea\u767a\u7684\u306b\u4f55\u3082\u3084\u3089\u306a\u3044\u4eba\u306f\u30a8\u30f3\u30b8\u30cb\u30a2\u3068\u3057\u3066\u7d99\u7d9a\u7684\u306b\u50cd\u304f\u3053\u3068\u306f\u96e3\u3057\u3044\u3063\u3066\u8a71\u306a\u3093\u3067\u3057\u3087\u3046\u306d\u3002\u5143\u306e\u8a00\u8449\u306f\u5f37\u3044\u3067\u3059\u304c\u3002","timestamp":"2021-01-05T13:11:51+09:00","star_count":[],"user":{"name":"reincarnation777","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/reincarnation777/profile.png"},"tags":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/reincarnation777","is_private":false},{"star_count":[{"count":1,"color":"normal"}],"timestamp":"2021-01-05T13:06:49+09:00","comment":"\u307e\u3042\u300c\u79c1\u306e\u3088\u3046\u306aCTO\u306b\u306f\u306a\u308c\u306a\u3044\u3088\u300d\u3063\u3066\u8a00\u3044\u305f\u3044\u3093\u3060\u308d\u3046\u306d\u3002\u30e6\u30cb\u30b3\u30fc\u30f3\u306e\u300c\u30d2\u30b2\u3068\u30dc\u30a4\u30f3\u300d\u3060\u306d\u300230\u5e74\u524d\u306e\u66f2\u3060\u3088\uff1f","user":{"name":"neetfull","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/neetfull/profile.png"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/neetfull","is_private":false,"tags":[]},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/workingmanisdead/profile.png","name":"workingmanisdead"},"star_count":[],"comment":"\u6d88\u3048\u3066\u308b","timestamp":"2021-01-05T13:01:03+09:00","link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/workingmanisdead","is_private":false,"tags":[]},{"user":{"name":"momoirotan","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/momoirotan/profile.png"},"comment":"","timestamp":"2021-01-05T12:52:41+09:00","star_count":[],"link":"","tags":[],"is_private":false},{"star_count":[],"comment":"\u3053\u308c\u306f\u306a\u306b","timestamp":"2021-01-05T12:51:35+09:00","user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/nzxx/profile.png","name":"nzxx"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/nzxx","is_private":false,"tags":[]},{"is_private":false,"link":"","tags":[],"user":{"name":"si_mako","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/si_mako/profile.png"},"star_count":[],"timestamp":"2021-01-05T12:48:51+09:00","comment":""},{"link":"","tags":[],"is_private":false,"comment":"","timestamp":"2021-01-05T12:44:16+09:00","star_count":[],"user":{"name":"akira28","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/akira28/profile.png"}},{"link":"","tags":[],"is_private":false,"user":{"name":"geopolitics","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/geopolitics/profile.png"},"comment":"","timestamp":"2021-01-05T12:35:38+09:00","star_count":[]},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/Dragoonriders/profile.png","name":"Dragoonriders"},"timestamp":"2021-01-05T12:34:49+09:00","comment":"\u982d\u306f\u4f11\u3081\u306a\u3044\u3068\u3088\u304f\u50cd\u304b\u306a\u3044\u3088\u3002\u4ed6\u4eba\u306e\u547c\u5438\u3067\u306f\u306a\u304f\u5df1\u306e\u547c\u5438\u3067\u3044\u3053\u3046\u3002","star_count":[],"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/Dragoonriders","tags":[],"is_private":false},{"timestamp":"2021-01-05T12:28:35+09:00","comment":"","star_count":[],"user":{"name":"kattsuk2","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/kattsuk2/profile.png"},"link":"","is_private":false,"tags":["\u3042\u3068\u3067\u8aad\u3080"]},{"star_count":[],"comment":"","timestamp":"2021-01-05T12:20:09+09:00","user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/nowandzen/profile.png","name":"nowandzen"},"link":"","tags":["\u3042\u3068\u3067\u8aad\u3080"],"is_private":false},{"is_private":false,"link":"","tags":[],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/tyakoske/profile.png","name":"tyakoske"},"star_count":[],"timestamp":"2021-01-05T12:19:11+09:00","comment":""},{"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/takopons","tags":["job"],"is_private":false,"timestamp":"2021-01-05T12:05:46+09:00","comment":"\u5143\u30c4\u30a4\u6d88\u3048\u3066\u308b\u3002ZOZO\u306e\u4eca\u6751\u96c5\u5e78\u3055\u3093\u3068\u304b\u3044\u3046CTO\uff1f\u306f\u81ea\u5206\u306e\u8a00\u8449\u306b\u81ea\u4fe1\u3082\u8cac\u4efb\u3082\u6301\u3066\u306a\u3044CTO\u306a\u306e\u304b\u3002\u305d\u308c\u3068\u3082\u3001CEO\u306b\u6012\u3089\u308c\u305f\u304b\u3002","star_count":[{"color":"normal","count":11}],"user":{"name":"takopons","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/takopons/profile.png"}},{"tags":[],"link":"","is_private":false,"comment":"","timestamp":"2021-01-05T12:05:35+09:00","star_count":[],"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/debussy1201/profile.png","name":"debussy1201"}}]}
            """.trimIndent()))
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("").toString())
            .addConverterFactory(ObjectParameterConverterFactory)
            .addConverterFactory(
                @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
                Json.asConverterFactory("application/json".toMediaType())
            )
            .client(OkHttpClient.Builder().build())
            .build()
        val api = retrofit.create(BookmarkAPI::class.java)
        val service = BookmarkService(api)

        // 403
        runCatching {
            service.getRecentBookmarks(url = testUrl)
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            println(it.toString())
            Assert.fail()
        }

        // 404
        runCatching {
            service.getRecentBookmarks(url = testUrl)
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            Assert.fail()
        }

        // 200
        service.getRecentBookmarks(url = testUrl).also {
            val request = server.takeRequest()
            println(request.path)
            assertEquals(null, request.failure)
            assertEquals(
                "/api/ipad.entry_bookmarks_with_cursor?url=${URLEncoder.encode(testUrl, "UTF-8")}",
                request.path
            )
        }.let { body ->
            println(body.cursor)
            body.bookmarks.forEach { b ->
                println(Json.encodeToString(b))
            }
        }

        server.shutdown()
    }

    @Test
    fun getBookmarksDigest() = runBlocking {
        val (server, hatenaApi) = createMock {
            enqueue(MockResponse().setResponseCode(403))
            enqueue(MockResponse().setResponseCode(404))
            enqueue(
                MockResponse().setBody("""
                {"refered_blog_entries":[],"favorite_bookmarks":[],"scored_bookmarks":[{"tags":[],"star_count":[{"color":"normal","count":182}],"comment":"\u3053\u3046\u3044\u3046\u306e\u306e\u305b\u3044\u306a\u6c17\u304c\u3059\u308b\u3088\u3002\u4eba\u8db3\u308a\u306a\u3044\u3068\u304b\u8a00\u3046\u306e\u3002","timestamp":"2021-01-04T20:41:39+09:00","is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/necDK","user":{"name":"necDK","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/necDK/profile.png"}},{"user":{"name":"damedom","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/damedom/profile.png"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/damedom","is_private":false,"timestamp":"2021-01-04T22:24:04+09:00","comment":"\u3053\u3046\u3044\u3046\u306e\u3001\u300c\u30a8\u30f3\u30b8\u30cb\u30a2\u3068\u3057\u3066\u4e00\u8a00\u8a00\u3044\u305f\u3044\u8001\u5bb3\u3057\u3050\u3055\u300d\uff0b\u300c\u696d\u52d9\u6642\u9593\u5916\u3067\u52dd\u624b\u306b\u52c9\u5f37\u3055\u305b\u3066\u305d\u306e\u6210\u679c\u3060\u3051\u304b\u3059\u3081\u53d6\u308a\u305f\u3044\u7d4c\u55b6\u8005\u3057\u3050\u3055\u300d\u3060\u3068\u601d\u3063\u306610\u5272\u5f15\u304d\u3067\u8aad\u3093\u3067\u308b","star_count":[{"count":1,"color":"red"},{"color":"normal","count":175}],"tags":[]},{"tags":[],"comment":"\u4f11\u307f\u306f\u4f11\u3080\u305f\u3081\u306b\u3042\u308b\u3093\u3058\u3083\uff01","star_count":[{"count":139,"color":"normal"}],"timestamp":"2021-01-04T20:48:12+09:00","is_private":false,"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/nori__3","user":{"name":"nori__3","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/nori__3/profile.png"}},{"comment":"\u300c\u3007\u3007\u3067\u304d\u306a\u3044\u3084\u3064\u306f\u3007\u3007\u3058\u3083\u306a\u3044\u3002\u300d\u3053\u3046\u3044\u3046\u8001\u5bb3\u3057\u3050\u3055\u304c\u59cb\u307e\u308b\u3068\u300c\u52dd\u624b\u306b\u8a00\u3063\u3066\u308d\u300d\u3068\u4eba\u306f\u96e2\u308c\u3066\u3044\u304f\u3002\u5c11\u5b50\u5316\u3082\u4eba\u624b\u4e0d\u8db3\u3082\u305d\u308c\u304c\u4e00\u56e0\u3067\u3057\u3087\u3002\u4f55\u56de\u7e70\u308a\u8fd4\u3059\u306e\u304b\u3002","star_count":[{"color":"normal","count":146}],"tags":[],"is_private":false,"timestamp":"2021-01-04T21:07:08+09:00","link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/chimairav","user":{"name":"chimairav","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/chimairav/profile.png"}},{"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/korilog","user":{"name":"korilog","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/korilog/profile.png"},"tags":[],"star_count":[{"count":119,"color":"normal"}],"comment":"\u307e\u3041\u305d\u3046\u306a\u3093\u3060\u3051\u3069\u3001\u5b9f\u969b\u554f\u984cIT\u4eba\u6750\u3092\u5897\u3084\u3059\u306b\u306f\u8da3\u5473\u3067\u30b3\u30fc\u30c9\u66f8\u304f\u4eba\u3060\u3051\u3058\u3083\u8db3\u308a\u306a\u3044\u306e\u3067\u3001\u696d\u52d9\u4e2d\u3060\u3051\u3067\u6210\u9577\u3067\u304d\u308b\u3088\u3046\u306a\u74b0\u5883\u3084\u7814\u4fee\u30b7\u30b9\u30c6\u30e0\u306e\u6574\u5099\u304c\u5fc5\u8981\u3060\u3068\u601d\u3063\u3066\u3044\u308b\u3002\u719f\u7df4\u5175\u306b\u983c\u308b\u3093\u3058\u3083\u306a\u304f\u3066\u30b7\u30b9\u30c6\u30e0\u5316\u3057\u305f\u3044","timestamp":"2021-01-04T20:35:07+09:00","is_private":false},{"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/hiromi163","user":{"name":"hiromi163","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/hiromi163/profile.png"},"tags":["\u4ed5\u4e8b","\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0"],"comment":"\u81ea\u767a\u7684\u306b\u3084\u308b\u306e\u306f\u826f\u3044\u3093\u3060\u3051\u3069\u3001\u90e8\u4e0b\u306b\u79c1\u751f\u6d3b\u3067\u3082\u4ed5\u4e8b\u306b\u95a2\u9023\u3059\u308b\u52c9\u5f37\u3092\u5f37\u5236\u3059\u308b\u4e0a\u53f8\u306f\u6ec5\u3093\u3067\u307b\u3057\u3044\u3002","star_count":[{"color":"normal","count":81}],"timestamp":"2021-01-04T21:41:10+09:00","is_private":false},{"user":{"name":"manimoto","profile_image_url":"https://cdn.profile-image.st-hatena.com/users/manimoto/profile.png"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/manimoto","timestamp":"2021-01-04T21:46:56+09:00","is_private":false,"tags":[],"comment":"\u305d\u3093\u306akyuns\u6c0f\u306eGitHub\u306f\u5e74\u672b\u83491\u672c\u3069\u3053\u308d\u304b\u3001\u5e74\u9593\u901a\u3057\u3066\u307b\u307c\u751f\u3048\u3066\u306a\u3044\u3063\u3066\u3044\u3046\u3002\u307e\u3042GitHub\u306bPush\u3057\u3066\u306a\u3044\u3060\u3051\u3060\u3063\u305f\u308a\u3001\u793e\u5185\u30b3\u30fc\u30c9\u306f\u89e6\u3063\u3066\u305f\u308a\u3001\u30d7\u30ed\u30b0\u30e9\u30df\u30f3\u30b0\u306f\u3057\u306a\u3044CTO\u306a\u306e\u304b\u3082\u3057\u308c\u306a\u3044\u3051\u3069\u3002 https://github.com/kyuns","star_count":[{"count":87,"color":"normal"}]},{"comment":"IT\u30a8\u30f3\u30b8\u30cb\u30a2\u754c\u9688\u306f\u3053\u306e\u30ce\u30ea\u3067\u3044\u3064\u307e\u3067\u3084\u308b\u6c17\u306a\u306e\uff1f\u3053\u308c\u3092\u80a9\u66f8\u304d\u306e\u3042\u308b\u4eba\u304c\u3044\u3063\u305f\u3089\u3001\u571f\u65e5\u3082\u4f1a\u793e\u306b\u8a70\u3081\u3055\u305b\u3066\u79c1\u751f\u6d3b\u3092\u6368\u3066\u3055\u305b\u3066\u305f\u662d\u548c\u3068\u5909\u308f\u3089\u3093\u3088\u3002","star_count":[{"count":1,"color":"red"},{"count":76,"color":"normal"}],"tags":[],"is_private":false,"timestamp":"2021-01-04T22:19:28+09:00","link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/augsUK","user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/augsUK/profile.png","name":"augsUK"}},{"user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/sisya/profile.png","name":"sisya"},"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/sisya","timestamp":"2021-01-04T21:07:51+09:00","is_private":false,"tags":[],"star_count":[{"count":59,"color":"normal"}],"comment":"\u30a8\u30f3\u30b8\u30cb\u30a2\u306e\u7d20\u990a\u3068\u3057\u3066\u597d\u5947\u5fc3\u304c\u5fc5\u8981\u3068\u3044\u3046\u306e\u306f\u540c\u610f\u3060\u304c\u3001\u305d\u308c\u3092\u4e0a\u306e\u7acb\u5834\u304b\u3089\u62bc\u3057\u4ed8\u3051\u308b\u3088\u3046\u306a\u7269\u8a00\u3044\u306b\u306a\u3063\u3066\u3057\u307e\u3046\u3068\u4e00\u6c17\u306b\u30cf\u30e9\u30b9\u30e1\u30f3\u30c8\u5316\u3059\u308b\u306e\u3067\u3001\u80a9\u66f8\u3042\u308a\u306e\u5834\u3067\u306f\u8a00\u3046\u3079\u304d\u3067\u306f\u306a\u3044\u3068\u601d\u3046\u3002\u7121\u99c4\u306b\u30d8\u30a4\u30c8\u3092\u6e9c\u3081\u308b"},{"link":"https://b.hatena.ne.jp/entry/4696585028439762562/comment/Mayu_mic","user":{"profile_image_url":"https://cdn.profile-image.st-hatena.com/users/Mayu_mic/profile.png","name":"Mayu_mic"},"comment":"\u305f\u307e\u306b\u306f\u610f\u8b58\u7684\u306b\u96e2\u308c\u308b\u3053\u3068\u3082\u5927\u4e8b\u3060\u3068\u601d\u3046\u3051\u3069\u306d","star_count":[{"count":30,"color":"normal"}],"tags":[],"is_private":false,"timestamp":"2021-01-04T20:38:10+09:00"}]}
            """.trimIndent()))
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("").toString())
            .addConverterFactory(ObjectParameterConverterFactory)
            .addConverterFactory(
                @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
                Json.asConverterFactory("application/json".toMediaType())
            )
            .client(OkHttpClient.Builder().build())
            .build()
        val service = retrofit.create(BookmarkAPI::class.java)

        // 403
        runCatching {
            service.getBookmarksDigest(url = testUrl)
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            Assert.fail()
        }

        // 404
        runCatching {
            service.getBookmarksDigest(url = testUrl)
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            Assert.fail()
        }

        // 200
        service.getBookmarksDigest(url = testUrl).also { body ->
            val request = server.takeRequest()
            println(request.path)
            assertEquals(null, request.failure)
            assertEquals(
                "/api/ipad.entry_reactions?url=${URLEncoder.encode(testUrl, "UTF-8")}",
                request.path
            )

            println("referredBlogEntries")
            body.referredBlogEntries.forEach { b ->
                println(Json.encodeToString(b))
            }
            println("favoriteBookmarks")
            body.favoriteBookmarks.forEach { b ->
                println(Json.encodeToString(b))
            }
            println("scoredBookmarks")
            body.scoredBookmarks.forEach { b ->
                println(Json.encodeToString(b))
            }
        }

        server.shutdown()
    }

    @Test
    fun getBookmarksEntry() = runBlocking {
        HatenaClient.bookmark.getBookmarksEntry(testUrl).let {
            assert(it.id > 0)
            println("id = " + it.id)
            println("title = " + it.title)
            println("url = " + it.url)
            println("entryUrl = " + it.entryUrl)
            println("requestedUrl = " + it.requestedUrl)
            println("screenshot = " + it.screenshot)
            println("v=== tags ===v")
            it.tags.forEach { tag ->
                println("${tag.first}(${tag.second})")
            }
            println("^=== tags ===^")
            println("count = " + it.count)
            println("bookmarks.size = " + it.bookmarks.size)
            it.bookmarks.forEach { b ->
                println(Json.encodeToString(b))
            }
        }
    }

    @Test
    fun getBookmarksCount() = runBlocking {
        val count = HatenaClient.bookmark.getBookmarksCount("https://suihan74.github.io/posts/2021/02_04_00_satena_160/")
        assertEquals(1, count)
    }

    @Test
    fun getBookmarksCount_multiUrls() = runBlocking {
        val map = HatenaClient.bookmark.getBookmarksCount(listOf(
            "https://suihan74.github.io/hogehoge",
            "https://suihan74.github.io/posts/2021/02_04_00_satena_160/"
        ))
        assertEquals(0, map["https://suihan74.github.io/hogehoge"])
        assertEquals(1, map["https://suihan74.github.io/posts/2021/02_04_00_satena_160/"])
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun getBookmarksCount_distinct() = runBlocking {
        val urls = buildList {
            repeat (100) {
                add("https://localhost/")
            }
        }
        val map = HatenaClient.bookmark.getBookmarksCount(urls)
        assertEquals(1, map.size)
        println(map[urls[0]])
    }
    
    @Test
    fun getBookmarksCount_manyUrls() = runBlocking { 
        val urls = listOf(
            "http://blog.father.gedow.net/2012/10/23/linux-command-for-trouble",
            "http://blog.livedoor.jp/kensuu/archives/50785541.html",
            "http://chaos2ch.com/archives/2586693.html",
            "http://css-happylife.com/archives/2007/0115_1345.php",
            "http://edu-dev.net/2012/09/20/ted_7sites/",
            "http://hiroki-tkg.com/?p=452",
            "http://kachibito.net/web-service/twitter/165-twitter-tools.html",
            "http://problem-solver.hateblo.jp/entry/20070312/1173628642",
            "http://rajic.2chblog.jp/archives/51860823.html",
            "http://rajic.2chblog.jp/archives/51950856.html",
            "http://sago.livedoor.biz/archives/50251034.html",
            "http://www.bookscan.co.jp/",
            "http://www.checkpad.jp/",
            "http://www.earthinus.com/2011/06/simple-writing.html",
            "http://www.fx2ch.net/archives/36182537.html",
            "http://www.lastday.jp/2010/11/22/objective-c",
            "http://www.mtblue.org/pc/tips/speed_up_xp.php",
            "http://www.takke.jp/",
            "https://221b.jp/",
            "https://anond.hatelabo.jp/20090401200113",
            "https://anond.hatelabo.jp/20091026215137",
            "https://anond.hatelabo.jp/20110825105018",
            "https://anond.hatelabo.jp/20130809115823",
            "https://blog.mirakui.com/entry/20091230/1262158458",
            "https://blog.sixapart.jp/2013-04/nine-moments.html",
            "https://commte.net//archives/3433",
            "https://e0166nt.com/blog-entry-213.html",
            "https://gigazine.net/news/20060415_firefoxthunderbird/",
            "https://glassonion.hatenablog.com/entry/20100802/1280758789",
            "https://jbpress.ismedia.jp/articles/-/34428",
            "https://jp.techcrunch.com/",
            "https://keisan.casio.jp/",
            "https://komoko.hatenablog.com/entry/20110524/p1",
            "https://pixlr.com/editor/",
            "https://portalshit.net/2014/12/11/thought-on-own-house",
            "https://qiita.com/hirokidaichi/items/591ad96ab12938878fe1",
            "https://ushigyu.net/2012/07/05/list_of_sites_to_think_about_domestic_travel/",
            "https://www.cheap-delicious.com/entry/2014/10/14/170133",
            "https://www.colordic.org/",
            "https://www.colourlovers.com/",
            "https://www.designwalker.com/2008/12/free-stock.html",
            "https://www.furomuda.com/entry/20080410/1207806673",
            "https://www.insource.co.jp/businessbunsho/houkoku_by_insource.html",
            "https://www.itmedia.co.jp/bizid/articles/0607/24/news034.html",
            "https://www.mdn.co.jp/di/",
            "https://www.msng.info/archives/2010/11/happy_mac_apps_for_ex_windows_users.php",
            "https://www.rarejob.com/",
            "https://www.slideshare.net/",
            "https://www.slideshare.net/yuka2py/javascript-23768378",
            "https://www.slideshare.net/yutamorishige50/ss-41321443"
        )
        println("urls size = ${urls.size}")
        val map = HatenaClient.bookmark.getBookmarksCount(urls)
        assertEquals(urls.size, map.size)
        urls.forEach {
            println(it + " : " + map[it])
        }
    }

    @Test
    fun getBookmarksCount_contains_unknown_url() = runBlocking {
        val map = HatenaClient.bookmark.getBookmarksCount(listOf(
            "https://suihan74.github.io/",
            "hhhhttps://suihan74.github.io/posts/2021/02_04_00_satena_160/"
        ))
        println(Json.encodeToString(map))
    }

    // ------ //

    @Test
    fun getTweetsAndClicks() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(user = "suihan74", urls = listOf("https://www3.nhk.or.jp/news/html/20210718/k10013145581000.html"))
        println(Json.encodeToString(tweetsAndClicks))
    }

    @Test
    fun getTweetsAndClicks_multiple_urls() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(
            user = "suihan74",
            urls = listOf(
                "https://www3.nhk.or.jp/news/html/20210718/k10013145581000.html",
                "https://www3.nhk.or.jp/news/html/20210715/k10013141521000.html"
            )
        )
        println(Json.encodeToString(tweetsAndClicks))
    }

    @Test
    fun getTweetsAndClicks_multiple_users() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(
            users = listOf("suihan74", "satenatest"),
            url = "https://www3.nhk.or.jp/news/html/20210718/k10013145581000.html",
        )
        println(Json.encodeToString(tweetsAndClicks))
    }

    @Test
    fun getTweetsAndClicks_multiple_urls_contains_no_tweets() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(
            user = "suihan74",
            urls = listOf(
                "https://www3.nhk.or.jp/news/html/20210718/k10013145581000.html",
                "https://www3.nhk.or.jp/news/html/20210715/k10013141521000.html",
                "https://anond.hatelabo.jp/20210711034748"
            )
        )
        println(Json.encodeToString(tweetsAndClicks))
    }

    @Test
    fun getTweetsAndClicks_unknown_user() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(
            user = "suihan74444444444444444444444444",
            urls = listOf("https://www3.nhk.or.jp/news/html/20210718/k10013145581000.html")
        )
        println(Json.encodeToString(tweetsAndClicks))
    }

    @Test
    fun getTweetsAndClicks_empty_urls() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(
            user = "suihan74",
            urls = listOf()
        )
        assertEquals(emptyList<TweetsAndClicks>(), tweetsAndClicks)
    }

    @Test
    fun getTweetsAndClicks_empty_users() = runBlocking {
        val tweetsAndClicks = HatenaClient.bookmark.getTweetsAndClicks(
            users = listOf(),
            url = "https://www3.nhk.or.jp/news/html/20210718/k10013145581000.html"
        )
        assertEquals(emptyList<TweetsAndClicks>(), tweetsAndClicks)
    }

    @Test
    fun postBookmark() = runBlocking {
        val url = "https://suihan74.github.io/"
        val comment = "test"
        val client = HatenaClient.signIn(rk)
        val result = client.bookmark.postBookmark(url, comment, private = true)
        assertEquals(result.user, user)
        assertEquals(result.comment, comment)
    }

    @Test
    fun deleteBookmark() = runBlocking {
        val url = "https://suihan74.github.io/"
        val comment = "test"
        val client = HatenaClient.signIn(rk)
        client.bookmark.postBookmark(url, comment, private = true)
        client.bookmark.deleteBookmark(url = url)
    }

    @Test
    fun getPrivateBookmark() = runBlocking {
        val client = HatenaClient.signIn(rk)
        val bookmarkResult = client.bookmark.getBookmark(
            eid = 4735994197208831813,
            user = "suihan74"
        )
        assertNotNull(bookmarkResult)
        assertEquals(true, bookmarkResult!!.private)
    }

    @Test
    fun getPrivateBookmark_fromNoCertifiedClient() = runBlocking {
        val client = HatenaClient
        val bookmarkResult = client.bookmark.getBookmark(
            eid = 4735994197208831813,
            user = "suihan74"
        )
        assertNull(bookmarkResult)
    }

    @Test
    fun getPublicBookmark() = runBlocking {
        val client = HatenaClient.signIn(rk)
        val bookmarkResult = client.bookmark.getBookmark(
            eid = 4735775702463931269,
            user = "suihan74"
        )
        assertNotNull(bookmarkResult)
        assertEquals(false, bookmarkResult!!.private)
    }

    @Test
    fun getPublicBookmark_fromNoCertifiedClient() = runBlocking {
        val client = HatenaClient
        val bookmarkResult = client.bookmark.getBookmark(
            eid = 4735775702463931269,
            user = "suihan74"
        )
        assertNotNull(bookmarkResult)
        assertEquals(false, bookmarkResult!!.private)
    }
}