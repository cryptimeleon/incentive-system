package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.BulkResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

interface CryptoApiService {
    @POST("genesis")
    suspend fun retrieveGenesisSignatureFor(@Header("user-public-key") serializedPublicKey: String): Response<String>

    @POST("join-promotion")
    suspend fun runIssueJoin(
        @Header("join-request") joinRequest: String,
        @Header("promotion-id") promotionId: String
    ): Response<String>

    @POST("bulk-token-updates")
    suspend fun sendTokenUpdatesBatch(
        @Header("basket-id") basketId: UUID,
        @Body bulkRequestDto: BulkRequestDto
    ): Response<Unit>

    @POST("bulk-token-update-results")
    suspend fun retrieveTokenUpdatesResults(@Header("basket-id") basketId: UUID): Response<BulkResponseDto>
}
