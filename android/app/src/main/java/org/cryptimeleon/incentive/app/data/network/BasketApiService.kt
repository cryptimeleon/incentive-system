package org.cryptimeleon.incentive.app.data.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface BasketApiService {
    @GET("items")
    suspend fun getAllItems(): Response<List<NetworkShoppingItem>>

    @GET("reward-items")
    suspend fun getAllRewardItems(): Response<List<NetworkRewardItem>>

    @PUT("basket/items")
    suspend fun putItemToBasket(@Body networkBasketItemPutRequest: NetworkBasketItemPutRequest): Response<Unit>

    @GET("basket/new")
    suspend fun getNewBasket(): Response<UUID>

    // This endpoint is for developing only and will be replaced by some payment process in the future
    @POST("basket/pay-dev")
    suspend fun payBasket(@Header("basket-id") basketId: UUID): Response<Unit>

}

@Parcelize
data class NetworkRewardItem(val id: String, val title: String) : Parcelable

@Parcelize
data class NetworkShoppingItem(val id: String, val price: Int, val title: String) : Parcelable

@Parcelize
data class NetworkBasketItemPutRequest(val basketId: UUID, val count: Int, val itemId: String) :
    Parcelable
