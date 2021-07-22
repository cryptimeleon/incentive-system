package org.cryptimeleon.incentive.app.repository.basket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BasketDao {
    @Query("SELECT * FROM baskets LIMIT 1")
    fun getBasket(): Basket

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setBasket(basket: Basket)
}
