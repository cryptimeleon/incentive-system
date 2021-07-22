package org.cryptimeleon.incentive.app.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header


private const val BASE_URL = "https://incentives.cs.upb.de/issue/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface IssueJoinApiService {
    @GET("issue")
    suspend fun runIssueJoin(
        @Header("join-request") joinRequest: String,
        @Header("public-key") publicKey: String
    ): Response<String>
}

object IssueJoinApi {
    val retrofitService: IssueJoinApiService by lazy {
        retrofit.create(IssueJoinApiService::class.java)
    }
}
