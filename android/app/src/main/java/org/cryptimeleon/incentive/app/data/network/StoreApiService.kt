package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.incentive.app.domain.model.BulkRequestStoreDto
import org.cryptimeleon.incentive.app.domain.model.BulkResultStoreDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

interface StoreApiService {
    @GET("register-user-and-obtain-serialized-registration-coupon")
    suspend fun retrieveRegistrationCouponFor(
        @Header("user-public-key") serializedPublicKey: String,
        @Header("user-info") userData: String
    ): Response<String>

    @POST("bulk")
    suspend fun sendBulkRequest(@Body bulkDto: BulkRequestStoreDto): Response<Void>

    @GET("bulk-results")
    suspend fun retrieveBulkResponse(@Header("basket-id") basketId: UUID): Response<BulkResultStoreDto>
}
