package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.*
import org.cryptimeleon.incentive.app.data.database.basket.BasketDao
import org.cryptimeleon.incentive.app.data.database.basket.BasketEntity
import org.cryptimeleon.incentive.app.data.network.*
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import java.util.*

class BasketRepository(
    private val basketApiService: BasketApiService,
    private val basketDao: BasketDao,
) : IBasketRepository {

    override val basket = flow {
        val basketId = getActiveBasketId()

        if (basketId != null) {
            val networkBasket = basketApiService.getBasketContent(basketId).body()
            val shoppingItems = shoppingItems.first()
            if (networkBasket != null) {
                val basketItems = networkBasket.items.mapNotNull { entry ->
                    val foundItem =
                        shoppingItems.find { shoppingItem: ShoppingItem -> shoppingItem.id == entry.key }
                    if (foundItem != null) {
                        return@mapNotNull BasketItem(
                            UUID.fromString(foundItem.id),
                            foundItem.title,
                            foundItem.price,
                            entry.value
                        )
                    } else {
                        return@mapNotNull null
                    }
                }
                val basket = Basket(
                    networkBasket.basketId,
                    basketItems,
                    networkBasket.paid,
                    networkBasket.redeemed,
                    networkBasket.value
                )
                emit(basket)
            }
        }
    }

    override val shoppingItems: Flow<List<ShoppingItem>> = flow {
        // TODO cache in Database
        emit(
            basketApiService.getAllItems().body()!!.map { ShoppingItem(it.id, it.price, it.title) })
    }

    override suspend fun ensureActiveBasket(): Boolean {
        val basketId: UUID? = basketDao.getActiveBasketId()

        if (basketId == null || !basketApiService.getBasketContent(basketId).isSuccessful) {
            return createNewBasket()
        }
        return true
    }

    override suspend fun putItemIntoCurrentBasket(itemId: String, amount: Int): Boolean {
        val basketId = basketDao.getActiveBasketId() ?: return false
        val basketItem = NetworkBasketItem(basketId, amount, itemId)
        val putItemResponse = basketApiService.putItemToBasket(basketItem)
        return putItemResponse.isSuccessful
    }

    override suspend fun getBasketItem(itemId: String): ShoppingItem? {
        return shoppingItems.first().find { it.id == itemId }
    }

    private suspend fun getActiveBasketId(): UUID? {
        return basketDao.getActiveBasketId()
    }

    override suspend fun createNewBasket(): Boolean {
        val basketResponse = basketApiService.getNewBasket()
        return if (basketResponse.isSuccessful) {
            val basketEntity = BasketEntity(basketResponse.body()!!, true)

            // Make sure all other baskets are set to inactive
            basketDao.setAllInactive()
            basketDao.insertBasket(basketEntity)
            true
        } else {
            false
        }
    }

    override suspend fun discardCurrentBasket(delete: Boolean): Boolean {
        if (delete) {
            val basketId = basketDao.getActiveBasketId()
            if (basketId != null) {
                basketApiService.deleteBasket(basketId)
            }
        }
        return createNewBasket()
    }

    override suspend fun payCurrentBasket(): Boolean {
        val basket = basket.first()

        // Pay basket
        val payResponse =
            basketApiService.payBasket(NetworkPayBody(basket.basketId, basket.value))
        return if (payResponse.isSuccessful) {
            discardCurrentBasket()
            true
        } else {
            false
        }
    }

    suspend fun setBasketItemCount(itemId: UUID, count: Int): Boolean {
        val basketId = getActiveBasketId() ?: return false

        val response = if (count <= 0) {
            basketApiService.removeItemFromBasket(
                basketId,
                itemId.toString()
            )
        } else {
            basketApiService.putItemToBasket(
                NetworkBasketItem(
                    basketId,
                    count,
                    itemId.toString()
                )
            )
        }
        return response.isSuccessful
    }
}
