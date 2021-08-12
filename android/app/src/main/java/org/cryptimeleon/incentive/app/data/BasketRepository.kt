package org.cryptimeleon.incentive.app.data

import org.cryptimeleon.incentive.app.data.database.basket.Basket
import org.cryptimeleon.incentive.app.data.database.basket.BasketDao
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.BasketItem

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
        var basket: Basket? = basketDao.getBasket()

        if (basket == null || !basket.isActive || !basketApiService.getBasketContent(basket.basketId).isSuccessful) {
            val basketResponse = basketApiService.getNewBasket()
            if (basketResponse.isSuccessful) {
                basket = Basket(basketResponse.body()!!, true)

                // Make sure all other baskets are set to inactive
                basketDao.setAllInactive()
                basketDao.insertBasket(basket)
                return true
            }
            return false
        }
        return true
    }

    /**
     * Put an item with a given amount to the basket.
     *
     * @return true if successful
     */
    suspend fun putItemIntoCurrentBasket(amount: Int, barcode: String): Boolean {
        val basket = basketDao.getBasket() ?: return false
        val basketItem = BasketItem(basket.basketId, amount, barcode)
        val putItemResponse = basketApiService.putItemToBasket(basketItem)
        return putItemResponse.isSuccessful
    }
}