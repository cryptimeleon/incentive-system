package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.incentive.promotion.promotions.Promotion
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

interface PromotionApiService {

    @POST
    suspend fun runIssueJoin(
        @Header("join-request") joinRequest: String,
        @Header("public-key") publicKey: String
    ): Response<String>

    @POST()
    suspend fun runCreditEarn(
        @Header("basket-id") basketId: UUID,
        @Header("earn-request") serializedEarnRequest: String
    ): Response<String>

    @GET("")
    suspend fun getPromotions(): Response<List<Promotion>>
}