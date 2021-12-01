package org.cryptimeleon.incentive.app.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface BasketApiService {
    @GET("items")
    suspend fun getAllItems(): Response<List<NetworkShoppingItem>>

    @GET("items/{id}")
    suspend fun getItemById(
        @Path(
            value = "id",
            encoded = true
        ) id: String
    ): Response<NetworkShoppingItem>

    @PUT("basket/items")
    suspend fun putItemToBasket(@Body networkBasketItem: NetworkBasketItem): Response<Unit>

    @DELETE("basket/items")
    suspend fun removeItemFromBasket(
        @Header("basketId") basketId: UUID,
        @Query("itemId") itemId: String
    ): Response<Unit>

    @GET("basket/new")
    suspend fun getNewBasket(): Response<UUID>

    // This endpoint is for developing only and will be replaced by some payment process in the future
    @POST("basket/pay-dev")
    suspend fun payBasket(@Body networkPayBody: NetworkPayBody): Response<Unit>

    @GET("basket")
    suspend fun getBasketContent(@Header("basketId") basketId: UUID): Response<NetworkBasket>

    @DELETE("basket")
    suspend fun deleteBasket(@Header("basketId") basketId: UUID): Response<Unit>
}

data class NetworkPayBody(val basketId: UUID, val value: Int)

@Parcelize
data class NetworkShoppingItem(val id: String, val price: Int, val title: String) : Parcelable

@Parcelize
data class NetworkBasketItem(val basketId: UUID, val count: Int, val itemId: String) : Parcelable

data class NetworkBasket(
    @SerializedName("basketID") val basketId: UUID,
    @SerializedName("items") val items: Map<String, Int>,
    @SerializedName("paid") val paid: Boolean,
    @SerializedName("redeemRequest") val redeemRequest: String,
    @SerializedName("redeemed") val redeemed: Boolean,
    @SerializedName("value") val value: Int,
)
