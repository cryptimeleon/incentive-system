package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.incentive.app.domain.model.BulkRequestProviderDto
import org.cryptimeleon.incentive.app.domain.model.BulkResultsProviderDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

interface ProviderApiService {
    @GET("register-with-coupon")
    suspend fun retrieveRegistrationSignatureFor(@Header("registration-coupon") serializedRegistrationCoupon: String): Response<String>

    @POST("join-promotion")
    suspend fun runIssueJoin(
        @Header("join-request") joinRequest: String,
        @Header("promotion-id") promotionId: String
    ): Response<String>

    @POST("bulk")
    suspend fun bulkRequest(@Body request: BulkRequestProviderDto): Response<BulkResultsProviderDto>
}
