package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BasketDao {
    @Query("SELECT * FROM baskets LIMIT 1")
    fun observeBasketEntity(): Flow<BasketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBasketEntity(basketEntity: BasketEntity)

    @Query("SELECT * FROM `basket-items` ORDER BY itemId ASC")
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
