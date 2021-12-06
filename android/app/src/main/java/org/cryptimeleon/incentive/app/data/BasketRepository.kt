package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.data.database.basket.BasketDao
import org.cryptimeleon.incentive.app.data.database.basket.BasketEntity
import org.cryptimeleon.incentive.app.data.database.basket.BasketItemEntity
import org.cryptimeleon.incentive.app.data.database.basket.ShoppingItemEntity
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.NetworkBasketItem
import org.cryptimeleon.incentive.app.data.network.NetworkPayBody
import org.cryptimeleon.incentive.app.data.network.NetworkShoppingItem
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem

class BasketRepository(
    private val basketApiService: BasketApiService,
    private val basketDao: BasketDao,
) : IBasketRepository {

    override val basket = basketDao.observeBasketEntity()
        .combine(basketDao.observeBasketItemEntities()) { a: BasketEntity?, b: List<BasketItemEntity> ->
            if (a != null) basketEntityToBasket(a, b.map { basketItemEntityToItem(it) }) else null
        }

    override val shoppingItems: Flow<List<ShoppingItem>> =
        basketDao.observeShoppingItems()
            .map { items: List<ShoppingItemEntity> -> items.map { shoppingItemEntityToItem(it) } }

    override suspend fun refreshShoppingItems() {
        basketDao.insertShoppingItems(
            basketApiService.getAllItems().body()!!
                .map { networkShoppingItemToShoppingItemEntity(it) }
        )
    }

    // Load contents from basket into database
    override suspend fun refreshBasket() {
        val basket: Basket = basket.first()!!
        val shoppingItems: List<ShoppingItem> = shoppingItems.first()

        val networkBasket = basketApiService.getBasketContent(basket.basketId).body()!!

        val basketItems = networkBasket.items.mapNotNull { entry ->
            val foundItem =
                shoppingItems.find { shoppingItem: ShoppingItem -> shoppingItem.id == entry.key }
            return@mapNotNull if (foundItem != null) shoppingItemToBasketItemEntity(
                foundItem,
                entry.value
            ) else null
        }
        val updatedBasket = BasketEntity(
            basketId = networkBasket.basketId,
            paid = networkBasket.paid,
            redeemed = networkBasket.redeemed,
            value = networkBasket.value,
        )
        basketDao.setBasketEntity(updatedBasket)
        basketDao.deleteAllBasketItems()
        basketDao.insertBasketItems(basketItems)
    }

    override suspend fun ensureActiveBasket(): Boolean {
        val basket = basket.first()

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
            basketDao.insertBasketItems(listOf(basketItemToEntity(basketItem)))
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

    companion object Converter {
        fun basketItemToEntity(basketItem: BasketItem): BasketItemEntity =
            BasketItemEntity(
                itemId = basketItem.itemId,
                price = basketItem.price,
                title = basketItem.title,
                count = basketItem.count
            )

        fun basketItemEntityToItem(basketItemEntity: BasketItemEntity): BasketItem =
            BasketItem(
                itemId = basketItemEntity.itemId,
                price = basketItemEntity.price,
                title = basketItemEntity.title,
                count = basketItemEntity.count
            )

        fun shoppingItemToBasketItemEntity(
            shoppingItem: ShoppingItem,
            count: Int
        ): BasketItemEntity =
            BasketItemEntity(
                title = shoppingItem.title,
                itemId = shoppingItem.id,
                price = shoppingItem.price,
                count = count
            )

        fun basketToEntity(basket: Basket): BasketEntity =
            BasketEntity(
                basketId = basket.basketId,
                paid = basket.paid,
                redeemed = basket.redeemed,
                value = basket.value
            )

        fun basketEntityToBasket(
            basketEntity: BasketEntity,
            basketItems: List<BasketItem>
        ): Basket =
            Basket(
                basketId = basketEntity.basketId,
                paid = basketEntity.paid,
                value = basketEntity.value,
                redeemed = basketEntity.redeemed,
                items = basketItems
            )

        fun shoppingItemEntityToItem(shoppingItemEntity: ShoppingItemEntity): ShoppingItem =
            ShoppingItem(
                id = shoppingItemEntity.itemId,
                price = shoppingItemEntity.price,
                title = shoppingItemEntity.title
            )

        fun networkShoppingItemToShoppingItemEntity(networkShoppingItem: NetworkShoppingItem): ShoppingItemEntity =
            ShoppingItemEntity(
                networkShoppingItem.id,
                networkShoppingItem.price,
                networkShoppingItem.title
            )
    }
}
