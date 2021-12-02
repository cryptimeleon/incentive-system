package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem

@Dao
interface BasketDao {
    @Query("SELECT * FROM baskets LIMIT 1")
    fun observeBasketEntity(): Flow<BasketEntity?>

    @Query("SELECT * FROM `shopping-items`")
    fun observeBasketItemEntities(): Flow<List<BasketItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBasketEntity(basketEntity: BasketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putBasketItem(basketItemEntity: BasketItemEntity)

    @Delete
    suspend fun removeBasketItem(basketItemEntity: BasketItemEntity)

    fun observeBasket(): Flow<Basket?> =
        observeBasketEntity().combine(observeBasketItemEntities()) { a: BasketEntity?, b: List<BasketItemEntity> ->
            if (a!= null) {
                Basket(
                    basketId = a.basketId,
                    paid = a.paid,
                    redeemed = a.redeemed,
                    value = a.value,
                    items = b.map {
                        BasketItem(
                            itemId = it.shoppingItemId,
                            count = it.count,
                            price = it.price,
                            title = it.title
                        )
                    })
            } else {
                null
            }
        }
}
