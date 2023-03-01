package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.cryptimeleon.incentive.app.data.database.basket.*
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.NetworkBasketItem
import org.cryptimeleon.incentive.app.data.network.NetworkBasketItemPutRequest
import org.cryptimeleon.incentive.app.data.network.NetworkShoppingItem
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import timber.log.Timber

class BasketRepository(
    private val basketApiService: BasketApiService,
    private val basketDao: BasketDao,
) : IBasketRepository {

    override val basket: Flow<Basket?> = basketDao.observeBasketEntity()
        .combine(basketDao.observeBasketItemEntities())
        { a: BasketEntity?, b: List<BasketItemEntity> ->
            if (a != null) basketEntityToBasket(
                a,
                b.map { basketItemEntityToItem(it) }) else null
        }.flowOn(Dispatchers.Default)

    override val shoppingItems: Flow<List<ShoppingItem>>
        get() = basketDao.observeShoppingItems()
            .map { items: List<ShoppingItemEntity> -> items.map { shoppingItemEntityToItem(it) } }
            .flowOn(Dispatchers.Default)

    override val rewardItems: Flow<List<RewardItem>>
        get() = basketDao.observeRewardItems()
            .map { items: List<RewardItemEntity> -> items.map { RewardItem(it.id, it.title) } }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshShoppingItems() {
        basketDao.insertShoppingItems(
            basketApiService.getAllItems().body()!!
                .map { networkShoppingItemToShoppingItemEntity(it) }
        )
    }

    override suspend fun refreshRewardItems() {
        basketDao.insertRewardItems(
            basketApiService.getAllRewardItems().body()!!
                .map { RewardItemEntity(it.id, it.title) }
        )
    }

    // Load contents from basket into database
    override suspend fun refreshBasket() {
        val basket: Basket = basket.first()!!
        val networkBasket = basketApiService.getBasketContent(basket.basketId).body()!!

        val updatedBasket = BasketEntity(
            basketId = networkBasket.basketId,
            paid = networkBasket.paid,
            redeemed = networkBasket.redeemed,
        )
        basketDao.setBasketEntity(updatedBasket)

        val basketItems = networkBasket.basketItems.map { networkBasketItemToBasketItemEntity(it) }
        basketDao.deleteAllBasketItems()
        basketDao.insertBasketItems(basketItems)
    }

    private fun networkBasketItemToBasketItemEntity(it: NetworkBasketItem) =
        BasketItemEntity(it.id, it.title, it.price, it.count)

    override suspend fun ensureActiveBasket() {
        if (needNewBasket()) {
            createNewBasket()
        }
    }

    private suspend fun needNewBasket(): Boolean {
        val basket = basket.first()
        return basket == null || !basketApiService.getBasketContent(basket.basketId).isSuccessful
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
        val networkBasketItem = NetworkBasketItemPutRequest(
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

    override suspend fun createNewBasket() {
        val createBasketResponse = basketApiService.getNewBasket()
        if (!createBasketResponse.isSuccessful) throw Exception("Could not create basket")
        val basketID = createBasketResponse.body()!!
        val basket = Basket(
            value = 0,
            redeemed = false,
            paid = false,
            basketId = basketID,
            items = listOf()
        )
        basketDao.setBasketEntity(basketToEntity(basket))
    }

    override suspend fun putItemIntoCurrentBasket(itemId: String, amount: Int): Boolean {
        // TODO or query shopping items
        val shoppingItem = shoppingItems.first().find { it.id == itemId } ?: return false
        putItemIntoCurrentBasket(shoppingItem = shoppingItem, amount = amount)
        return true
    }

    override suspend fun discardCurrentBasket(delete: Boolean) {
        val basket = basket.first()
        if (delete && basket != null) {
            basketApiService.deleteBasket(basket.basketId)
        }
        basketDao.deleteAllBasketItems()
        createNewBasket()
    }

    override suspend fun payCurrentBasket() {
        val basket = basket.first()

        // Pay basket
        val payResponse =
            basketApiService.payBasket(basket!!.basketId)
        if (!payResponse.isSuccessful) {
            Timber.e(payResponse.raw().toString())
        }
    }

    companion object Converter {
        fun basketItemToEntity(basketItem: BasketItem): BasketItemEntity =
            BasketItemEntity(
                itemId = basketItem.itemId,
                title = basketItem.title,
                price = basketItem.price,
                count = basketItem.count
            )

        fun basketItemEntityToItem(basketItemEntity: BasketItemEntity): BasketItem =
            BasketItem(
                itemId = basketItemEntity.itemId,
                price = basketItemEntity.price,
                title = basketItemEntity.title,
                count = basketItemEntity.count
            )

        fun basketToEntity(basket: Basket): BasketEntity =
            BasketEntity(
                basketId = basket.basketId,
                paid = basket.paid,
                redeemed = basket.redeemed,
            )

        fun basketEntityToBasket(
            basketEntity: BasketEntity,
            basketItems: List<BasketItem>
        ): Basket =
            Basket(
                basketId = basketEntity.basketId,
                paid = basketEntity.paid,
                value = basketItems.map { it.count * it.price }.sum(),
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
