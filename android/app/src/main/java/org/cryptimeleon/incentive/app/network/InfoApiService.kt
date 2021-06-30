package org.cryptimeleon.incentive.app.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://incentives.cs.upb.de/info/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface InfoApiService {
    @GET("public-parameters")
    suspend fun getPublicParameters(): Response<String>

    @GET("provider-public-key")
    suspend fun getProviderPublicKey(): Response<String>
}

object InfoApi {
    val retrofitService: InfoApiService by lazy {
        retrofit.create(InfoApiService::class.java)
    }
}
