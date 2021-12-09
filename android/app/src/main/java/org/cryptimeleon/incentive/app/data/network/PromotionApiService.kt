package org.cryptimeleon.incentive.app.data.network

import retrofit2.Response
import retrofit2.http.GET

interface PromotionApiService {
    @GET("promotions")
    suspend fun getPromotions(): Response<List<String>>
}