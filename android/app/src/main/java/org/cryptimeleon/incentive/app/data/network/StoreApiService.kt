package org.cryptimeleon.incentive.app.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface StoreApiService {
    @GET("register-user-and-obtain-serialized-registration-coupon")
    suspend fun retrieveRegistrationCouponFor(
        @Header("user-public-key") serializedPublicKey: String,
        @Header("user-info") userData: String
    ): Response<String>
}