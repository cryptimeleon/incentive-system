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

    @Test
    @Throws(Exception::class)
    fun testBasket() = runBlocking {
        val basketFlow = basketDao.observeBasket()
        Assert.assertNull(basketFlow.first())

        val basket = BasketEntity(
            basketId = UUID.randomUUID(),
            value = 10,
            redeemed = false,
            paid = false,
        )
        basketDao.setBasketEntity(basket)
        Assert.assertNotNull(basketFlow.first())

        val firstBasketItemEntity = BasketItemEntity(
            "first-item",
            199,
            "Chocolate",
            4
        )
        val secondBasketItemEntity = BasketItemEntity(
            "second-item",
            99,
            "Banana",
            2
        )
        basketDao.putBasketItem(firstBasketItemEntity)
        Assert.assertEquals(1, basketFlow.first()!!.items.size)

        basketDao.putBasketItem(secondBasketItemEntity)
        Assert.assertEquals(2, basketFlow.first()!!.items.size)

        basketDao.putBasketItem(firstBasketItemEntity.copy(count = 5))
        Assert.assertEquals(
            5,
            basketFlow.first()!!.items.find { it.itemId == firstBasketItemEntity.shoppingItemId }!!.count
        )

        basketDao.removeBasketItem(firstBasketItemEntity)
        Assert.assertEquals(
            1,
            basketFlow.first()!!.items.size
        )
    }
}