package org.cryptimeleon.incentive.app.data

import org.cryptimeleon.incentive.app.basket.BasketListItem
import org.cryptimeleon.incentive.app.data.database.basket.BasketDao
import org.cryptimeleon.incentive.app.data.database.basket.BasketEntity
import org.cryptimeleon.incentive.app.data.network.Basket
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.BasketItem
import org.cryptimeleon.incentive.app.data.network.Item
import org.cryptimeleon.incentive.app.data.network.PayBody
import java.util.UUID

class BasketRepository(
    private val basketApiService: BasketApiService,
    private val basketDao: BasketDao,
) {
    /**
     * If there is no (active) basket, this function will create a new basket.
     *
     * @return true if the function was successful and an active basket can be assumed to exist.
     */
    suspend fun ensureActiveBasket(): Boolean {
        val basketId: UUID? = basketDao.getActiveBasketId()

        if (basketId == null || !basketApiService.getBasketContent(basketId).isSuccessful) {
            return createNewBasket()
        }
        return true
    }

    /**
     * Put an item with a given amount to the basket.
     *
     * @return true if successful
     */
    suspend fun putItemIntoCurrentBasket(amount: Int, barcode: String): Boolean {
        val basketId = basketDao.getActiveBasketId() ?: return false
        val basketItem = BasketItem(basketId, amount, barcode)
        val putItemResponse = basketApiService.putItemToBasket(basketItem)
        return putItemResponse.isSuccessful
    }

    suspend fun getBasketItem(barcode: String): Item? {
        return basketApiService.getItemById(barcode).body()
    }

    /**
     * Get the currently active basket.
     */
    suspend fun getActiveBasketId(): UUID? {
        return basketDao.getActiveBasketId()
    }

    // TODO Flow, maybe combine these two into a more detailed basket object
    suspend fun getActiveBasket(): Basket? {
        val basketId = getActiveBasketId() ?: return null
        return basketApiService.getBasketContent(basketId).body()
    }

    // TODO Flow
    suspend fun getCurrentBasketContents(): List<BasketListItem>? {
        val basketId = basketDao.getActiveBasketId() ?: return null
        val getBasketResponse = basketApiService.getBasketContent(basketId)
        return if (getBasketResponse.isSuccessful) {
            val basket: Basket = getBasketResponse.body()!!

            val itemsInBasket = ArrayList<BasketListItem>()
            basket.items.forEach { (id, count) ->
                val item = basketApiService.getItemById(id)
                itemsInBasket.add(
                    BasketListItem(
                        item.body()!!,
                        count
                    )
                )
            }
            itemsInBasket
        } else {
            null
        }
    }

    /**
     * Creates a new basket and invalidates all other baskets
     */
    private suspend fun createNewBasket(): Boolean {
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

    /**
     * Discards the current basket and creates a new basket.
     *
     * @return true if successful
     */
    suspend fun discardCurrentBasket(delete: Boolean = false): Boolean {
        if (delete) {
            val basketId = basketDao.getActiveBasketId()
            if (basketId != null) {
                basketApiService.deleteBasket(basketId)
            }
        }
        return createNewBasket()
    }

    /**
     * Pays the current basket.
     *
     * @return true if basket is paid after finished
     */
    suspend fun payCurrentBasket(): Boolean {
        val basket = getActiveBasket() ?: return false

        // Pay basket
        val payResponse =
            basketApiService.payBasket(PayBody(basket.basketId, basket.value))
        return if (payResponse.isSuccessful) {
            discardCurrentBasket()
            true
        } else {
            false
        }
    }

    suspend fun setBasketItemCount(itemId: String, count: Int): Boolean {
        val basketId = getActiveBasketId() ?: return false

        val response = if (count <= 0) {
            basketApiService.removeItemFromBasket(
                basketId,
                itemId
            )
        } else {
            basketApiService.putItemToBasket(
                BasketItem(
                    basketId,
                    count,
                    itemId
                )
            )
        }
        return response.isSuccessful
    }
}
