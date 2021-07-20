package org.cryptimeleon.incentive.app.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.*


private const val BASE_URL = "https://incentives.cs.upb.de/basket/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface BasketApiService {
    @GET("items")
    suspend fun getAllItems(): Response<List<Item>>
}

data class Item(val id: UUID, val price: Int, val title: String)

object BasketApi {
    val retrofitService: BasketApiService by lazy {
        retrofit.create(BasketApiService::class.java)
    }
}
