package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem

interface IBasketRepository {

    val basket: Flow<Basket?>
    val shoppingItems: Flow<List<ShoppingItem>>
    val rewardItems: Flow<List<RewardItem>>

    /**
     * If there is no (active) basket, this function will create a new basket.
     *
     * @return true if the function was successful and an active basket can be assumed to exist.
     */
    suspend fun ensureActiveBasket(): Boolean

    /**
     * Creates a new basket and invalidates all other baskets
     */
    suspend fun createNewBasket(): Boolean

    /**
     * Put an item with a given amount to the basket.
     *
     * @return true if successful
     */
    suspend fun putItemIntoCurrentBasket(itemId: String, amount: Int): Boolean

    suspend fun getBasketItem(itemId: String): ShoppingItem?

    /**
     * Discards the current basket and creates a new basket.
     * @param delete only deletes basket on server side if delete set to true
     * @return true if successful
     */
    suspend fun discardCurrentBasket(delete: Boolean = false): Boolean

    /**
     * Pays the current basket.
     */
    suspend fun payCurrentBasket()

    /**
     * Load basket contents from basket server and update it in the database accordingly
     */
    suspend fun refreshBasket()

    /**
     * Load shopping items from basket server and update them in the database accordingly
     */
    suspend fun refreshShoppingItems()

    /**
     * Load reward items from basket server and update them in the database accordingly
     */
    suspend fun refreshRewardItems()
}
