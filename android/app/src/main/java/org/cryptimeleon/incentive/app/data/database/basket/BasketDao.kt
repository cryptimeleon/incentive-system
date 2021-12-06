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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBasketEntity(basketEntity: BasketEntity)

    @Query("SELECT * FROM `basket-items`")
    fun observeBasketItemEntities(): Flow<List<BasketItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBasketItems(basketItemsEntity: List<BasketItemEntity>)

    @Delete
    suspend fun removeBasketItem(basketItemEntity: BasketItemEntity)

    @Query("DELETE FROM `basket-items`")
    suspend fun deleteAllBasketItems()

    @Query("SELECT * FROM `shopping-items`")
    fun observeShoppingItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItems(shoppingItemsEntity: List<ShoppingItemEntity>)

    @Query("DELETE FROM `shopping-items`")
    suspend fun deleteAllShoppingItems()
}
