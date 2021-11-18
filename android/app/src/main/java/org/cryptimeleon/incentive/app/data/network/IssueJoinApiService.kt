package org.cryptimeleon.incentive.app.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface IssueJoinApiService {
    @GET("issue")
    suspend fun runIssueJoin(
        @Header("join-request") joinRequest: String,
        @Header("public-key") publicKey: String
    ): Response<String>
}
