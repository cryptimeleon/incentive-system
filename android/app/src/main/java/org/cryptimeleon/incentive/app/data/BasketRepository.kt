package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.cryptimeleon.incentive.app.data.database.basket.BasketDao
import org.cryptimeleon.incentive.app.data.database.basket.BasketEntity
import org.cryptimeleon.incentive.app.data.database.basket.BasketItemEntity
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.NetworkBasketItem
import org.cryptimeleon.incentive.app.data.network.NetworkPayBody
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem

class BasketRepository(
    private val basketApiService: BasketApiService,
    private val basketDao: BasketDao,
) : IBasketRepository {

    override val basket = basketDao.observeBasket()

    override val shoppingItems: Flow<List<ShoppingItem>> = flow {
        // TODO cache in Database
        emit(
            basketApiService.getAllItems().body()!!.map { ShoppingItem(it.id, it.price, it.title) })
    }

    override suspend fun ensureActiveBasket(): Boolean {
        val basket = basketDao.observeBasket().first()

        if (basket == null || !basketApiService.getBasketContent(basket.basketId).isSuccessful) {
            return createNewBasket()
        }
        return true
    }

    private suspend fun putItemIntoCurrentBasket(shoppingItem: ShoppingItem, amount: Int): Boolean {
        // Update database, Trigger request, refresh database
        val basket: Basket = basket.first() ?: return false

        // Update Database
        val basketItem = BasketItem(
            itemId = shoppingItem.id,
            title = shoppingItem.title,
            price = shoppingItem.price,
            count = amount
        )
        if (amount == 0) {
            basketDao.removeBasketItem(basketItemToEntity(basketItem))
        } else {
            basketDao.putBasketItem(basketItemToEntity(basketItem))
        }

        // Trigger request
        val networkBasketItem = NetworkBasketItem(
            basketId = basket.basketId,
            count = amount,
            itemId = shoppingItem.id
        )
        val putItemResponse = basketApiService.putItemToBasket(networkBasketItem)

        // TODO update database if not successful
        return putItemResponse.isSuccessful
    }

    override suspend fun getBasketItem(itemId: String): ShoppingItem? {
        return shoppingItems.first().find { it.id == itemId }
    }

    override suspend fun createNewBasket(): Boolean {
        val createBasketResponse = basketApiService.getNewBasket()
        if (!createBasketResponse.isSuccessful) return false
        val basketID = createBasketResponse.body()!!
        val basket = Basket(
            value = 0,
            redeemed = false,
            paid = false,
            basketId = basketID,
            items = listOf()
        )
        basketDao.setBasketEntity(basketToEntity(basket))
        return true
    }

    override suspend fun putItemIntoCurrentBasket(itemId: String, amount: Int): Boolean {
        // TODO or query shopping items
        val shoppingItem = shoppingItems.first().find { it.id == itemId } ?: return false
        putItemIntoCurrentBasket(shoppingItem = shoppingItem, amount = amount)
        return true
    }

    override suspend fun discardCurrentBasket(delete: Boolean): Boolean {
        val basket = basket.first()
        if (basket != null) {
            basketApiService.deleteBasket(basket.basketId)
        }
        return createNewBasket()
    }

    override suspend fun payCurrentBasket(): Boolean {
        val basket = basket.first() ?: return false

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

    companion object Converter{
        fun basketItemToEntity(basketItem: BasketItem): BasketItemEntity =
            BasketItemEntity(
                shoppingItemId = basketItem.itemId,
                price = basketItem.price,
                title = basketItem.title,
                count = basketItem.count
            )

        fun basketItemEntityToItem(basketItemEntity: BasketItemEntity): BasketItem =
            BasketItem(
                itemId = basketItemEntity.shoppingItemId,
                price = basketItemEntity.price,
                title = basketItemEntity.title,
                count = basketItemEntity.count
            )

        fun basketToEntity(basket: Basket): BasketEntity =
            BasketEntity(
                basketId = basket.basketId,
                paid = basket.paid,
                redeemed = basket.redeemed,
                value = basket.value
            )    }
}
