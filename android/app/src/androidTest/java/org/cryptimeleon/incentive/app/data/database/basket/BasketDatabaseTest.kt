package org.cryptimeleon.incentive.app.data.database.basket

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class BasketDatabaseTest {
    private lateinit var basketDao: BasketDao
    private lateinit var db: BasketDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, BasketDatabase::class.java
        ).build()
        basketDao = db.basketDatabaseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private val basket = BasketEntity(
        basketId = UUID.randomUUID(),
        value = 10,
        redeemed = false,
        paid = false,
    )

    @Test
    @Throws(Exception::class)
    fun testBasket() = runBlocking {
        val basketFlow = basketDao.observeBasketEntity()
        Assert.assertNull(basketFlow.first())
        basketDao.setBasketEntity(basket)
        Assert.assertEquals(basket, basketFlow.first())
    }

    private val firstBasketItemEntity = BasketItemEntity(
        "first-item",
        199,
        "Chocolate",
        4
    )

    private val secondBasketItemEntity = BasketItemEntity(
        "second-item",
        99,
        "Banana",
        2
    )

    @Test
    @Throws(Exception::class)
    fun testBasketItems() = runBlocking {
        val basketItemsFlow = basketDao.observeBasketItemEntities()
        Assert.assertEquals(0, basketItemsFlow.first().size)
        basketDao.insertBasketItems(listOf(firstBasketItemEntity, secondBasketItemEntity))
        Assert.assertEquals(2, basketItemsFlow.first().size)
        basketDao.insertBasketItems(listOf(firstBasketItemEntity.copy(count = 5)))
        Assert.assertEquals(
            5,
            basketItemsFlow.first().find { it.itemId == firstBasketItemEntity.itemId }!!.count
        )

        basketDao.removeBasketItem(firstBasketItemEntity)
        Assert.assertEquals(1, basketItemsFlow.first().size)
        Assert.assertEquals(secondBasketItemEntity, basketItemsFlow.first()[0])
        basketDao.deleteAllBasketItems()
        Assert.assertEquals(0, basketItemsFlow.first().size)
    }


    private val firstShoppingItemEntity = ShoppingItemEntity(
        "first", 199, "Potato"
    )
    private val secondShoppingItemEntity = ShoppingItemEntity(
        "second", 499, "Chocolate Cake"
    )

    @Test
    @Throws(Exception::class)
    fun testShoppingItems() = runBlocking {
        val shoppingItemsFlow= basketDao.observeShoppingItems()
        Assert.assertEquals(0, shoppingItemsFlow.first().size)
        basketDao.insertShoppingItems(listOf(firstShoppingItemEntity, secondShoppingItemEntity))
        Assert.assertEquals(2, shoppingItemsFlow.first().size)
        basketDao.insertShoppingItems(listOf(firstShoppingItemEntity.copy(price = 149)))
        Assert.assertEquals(
            149,
            shoppingItemsFlow.first().find { it.itemId == firstShoppingItemEntity.itemId }!!.price
        )

        basketDao.deleteAllShoppingItems()
        basketDao.deleteAllBasketItems()
        Assert.assertEquals(0, shoppingItemsFlow.first().size)
    }
}