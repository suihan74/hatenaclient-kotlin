package com.suihan74.hatena.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GeneralService {
    /**
     * 指定URLにGETリクエストを送る
     */
    @GET
    suspend fun get(@Url url : String) : Response<ResponseBody>
}