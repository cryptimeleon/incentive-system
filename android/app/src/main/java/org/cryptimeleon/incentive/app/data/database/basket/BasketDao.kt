package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.*
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

    // Shopping items are not connected to basket, just a cache that mirrors all items a user can scan

    @Query("SELECT * FROM `shopping-items`")
    fun observeShoppingItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItems(shoppingItemEntities: List<ShoppingItemEntity>)

    @Query("DELETE FROM `shopping-items`")
    suspend fun deleteAllShoppingItems()

    // Reward items are not connected to basket, just a cache that mirrors all rewards
    @Query("SELECT * FROM `reward-items`")
    fun observeRewardItems(): Flow<List<RewardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardItems(rewardItemEntities: List<RewardItemEntity>)

    @Query("DELETE FROM `reward-items`")
    suspend fun deleteAllRewardItems()
}
