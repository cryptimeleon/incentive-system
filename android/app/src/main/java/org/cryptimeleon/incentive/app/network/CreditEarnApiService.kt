package org.cryptimeleon.incentive.app.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import java.util.*


interface CreditEarnApiService {
    @GET("credit")
    suspend fun runCreditEarn(
        @Header("basket-id") basketId: UUID,
        @Header("earn-request") serializedEarnRequest: String
    ): Response<String>
}
