package org.cryptimeleon.incentive.app.data.network

import retrofit2.Response
import retrofit2.http.GET

interface InfoApiService {
    @GET("public-parameters")
    suspend fun getPublicParameters(): Response<String>

    @GET("provider-public-key")
    suspend fun getProviderPublicKey(): Response<String>
}
