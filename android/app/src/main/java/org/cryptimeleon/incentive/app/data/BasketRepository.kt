package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.cryptimeleon.incentive.app.data.database.basket.*
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.NetworkBasketItemPutRequest
import org.cryptimeleon.incentive.app.data.network.NetworkShoppingItem
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import timber.log.Timber
import java.util.*

class BasketRepository(
    private val basketApiService: BasketApiService,
    private val basketDao: BasketDao,
) : IBasketRepository {

    override val basket: Flow<Basket> = basketDao.observeBasketItemEntities()
        .map { b: List<BasketItemEntity> ->
            basketEntityToBasket(b.map { basketItemEntityToItem(it) })
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

    override suspend fun getBasketItem(itemId: String): ShoppingItem? {
        return shoppingItems.first().find { it.id == itemId }
    }

    override suspend fun putItemIntoBasket(itemId: String, amount: Int) {
        val shoppingItem = shoppingItems.first().find { it.id == itemId }
        if (shoppingItem == null) {
            Timber.e("could not find shopping item with id $itemId")
            return
        }

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
    }

    override suspend fun discardCurrentBasket() {
        basketDao.deleteAllBasketItems()
    }

    override suspend fun pushCurrentBasket(): UUID {
        val createBasketResponse = basketApiService.getNewBasket()
        if (!createBasketResponse.isSuccessful || createBasketResponse.body() == null) {
            throw Exception("Could not create basket!")
        }

        val basketId = createBasketResponse.body()!!
        basketDao.observeBasketItemEntities().first().forEach {basketItemEntity ->
            val request = NetworkBasketItemPutRequest(basketId, basketItemEntity.count, basketItemEntity.itemId)
            val response = basketApiService.putItemToBasket(request)
            if (!response.isSuccessful) Timber.e("An error occurred when putting \n$basketItemEntity\nto current basket.")
        }
        return basketId
    }

    override suspend fun payBasket(basketId: UUID) {
        val payResponse =
            basketApiService.payBasket(basketId)
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

        fun basketEntityToBasket(
            basketItems: List<BasketItem>
        ): Basket =
            Basket(
                items = basketItems,
                value = basketItems.sumOf { it.count * it.price },
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
