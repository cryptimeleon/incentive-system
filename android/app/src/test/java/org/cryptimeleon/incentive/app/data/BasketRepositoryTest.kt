package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.data.database.basket.BasketDao
import org.cryptimeleon.incentive.app.data.database.basket.BasketItemEntity
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class BasketRepositoryTest {

    private lateinit var basketRepository: BasketRepository

    @Mock
    private lateinit var basketDao: BasketDao

    @Mock
    private lateinit var basketApiService: BasketApiService

    private val testBasketItemEntity = BasketItemEntity("some-item-id", "some item", 299, 5)

    /**
     * Test combination of flows from database to basket object
     */
    @Test
    fun getBasket() = runBlocking {
        val basketItemsEntityFlow = MutableStateFlow<List<BasketItemEntity>>(emptyList())

        given(basketDao.observeBasketItemEntities()).willReturn(basketItemsEntityFlow)

        basketRepository = BasketRepository(basketApiService, basketDao)

        val basketFlow = basketRepository.basket
        assertNull(basketFlow.first())

        assertEquals(
            BasketRepository.basketEntityToBasket(emptyList()),
            basketFlow.first()
        )

        basketItemsEntityFlow.emit(listOf(testBasketItemEntity))
        assertEquals(
            BasketRepository.basketEntityToBasket(
                listOf(BasketRepository.basketItemEntityToItem(testBasketItemEntity))
            ), basketFlow.first()
        )
    }
}
