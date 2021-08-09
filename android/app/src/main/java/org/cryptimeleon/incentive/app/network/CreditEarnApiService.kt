package org.cryptimeleon.incentive.app.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.util.*


private const val BASE_URL = "https://incentives.cs.upb.de/credit/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface CreditEarnApiService {
    @GET("credit")
    suspend fun runCreditEarn(
        @Header("basket-id") basketId: UUID,
        @Header("earn-request") serializedEarnRequest: String
    ): Response<String>
}

object CreditEarnApi {
    val retrofitService: CreditEarnApiService by lazy {
        retrofit.create(CreditEarnApiService::class.java)
    }
}
