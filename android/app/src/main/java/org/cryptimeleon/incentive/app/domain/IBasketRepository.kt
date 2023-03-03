package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import java.util.*

interface IBasketRepository {

    val basket: Flow<Basket>
    val shoppingItems: Flow<List<ShoppingItem>>
    val rewardItems: Flow<List<RewardItem>>

    /**
     * Put an item with a given amount to the basket.
     */
    suspend fun putItemIntoBasket(itemId: String, amount: Int)

    suspend fun getBasketItem(itemId: String): ShoppingItem?

    /**
     * Discards the current basket.
     */
    suspend fun discardCurrentBasket()

    /**
     * Send current basket to store.
     */
    suspend fun pushCurrentBasket(): UUID

    /**
     * Pays the basket.
     */
    suspend fun payBasket(basketId: UUID)

    /**
     * Load shopping items from basket server and update them in the database accordingly
     */
    suspend fun refreshShoppingItems()

    /**
     * Load reward items from basket server and update them in the database accordingly
     */
    suspend fun refreshRewardItems()
}
