package org.cryptimeleon.incentive.app.data.network

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

interface CryptoApiService {
    @POST("join-promotion")
    suspend fun runIssueJoin(
        @Header("join-request") joinRequest: String,
        @Header("promotion-id") promotionId: String,
        @Header("user-public-key") publicKey: String
    ): Response<String>

    @POST()
    suspend fun runCreditEarn(
        @Header("basket-id") basketId: UUID,
        @Header("promotion-id") promotionId: Int,
        @Header("earn-request") serializedEarnRequest: String
    ): Response<String>
}