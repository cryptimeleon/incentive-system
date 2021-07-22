package org.cryptimeleon.incentive.app.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


private const val BASE_URL = "https://incentives.cs.upb.de/basket/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface BasketApiService {
    @GET("items")
    suspend fun getAllItems(): Response<List<Item>>

    @GET("items/{id}")
    suspend fun getItemById(@Path(value = "id", encoded = true) id: String): Response<Item>
}

@Parcelize
data class Item(val id: String, val price: Int, val title: String) : Parcelable

object BasketApi {
    val retrofitService: BasketApiService by lazy {
        retrofit.create(BasketApiService::class.java)
    }
}
