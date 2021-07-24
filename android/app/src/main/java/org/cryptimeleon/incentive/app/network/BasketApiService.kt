package org.cryptimeleon.incentive.app.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*


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

    @PUT("basket/items")
    suspend fun putItemToBasket(@Body basketItem: BasketItem): Response<Unit>

    @DELETE("basket/items")
    suspend fun removeItemFromBasket(
        @Header("basketId") basketId: UUID,
        @Query("itemId") itemId: String
    ): Response<Unit>

    @GET("basket/new")
    suspend fun getNewBasket(): Response<UUID>

    @GET("basket")
    suspend fun getBasketContent(@Header("basketId") basketId: UUID): Response<Basket>

    @DELETE("basket")
    suspend fun deleteBasket(@Header("basketId") basketId: UUID): Response<Unit>
}

@Parcelize
data class Item(val id: String, val price: Int, val title: String) : Parcelable

@Parcelize
data class BasketItem(val basketId: UUID, val count: Int, val itemId: String) : Parcelable

data class Basket(
    @SerializedName("basketID") val basketId: UUID,
    @SerializedName("items") val items: Map<String, Int>,
    @SerializedName("paid") val paid: Boolean,
    @SerializedName("redeemRequest") val redeemRequest: String,
    @SerializedName("redeemed") val redeemed: Boolean,
    @SerializedName("value") val value: Int,
)

object BasketApi {
    val retrofitService: BasketApiService by lazy {
        retrofit.create(BasketApiService::class.java)
    }
}
