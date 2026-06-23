package com.meirini.tbs_prediction.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("predict-tbs")
    fun getPrediksiTbs(
        @Body requestData: TbsRequest
    ): Call<TbsResponse>

}