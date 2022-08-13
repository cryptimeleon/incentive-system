package org.cryptimeleon.incentive.app.data.network

import retrofit2.Response
import retrofit2.http.GET

interface DosApiService {
    @GET("dos/short-duration")
    suspend fun launchShortDosAttack(): Response<Unit>
}
