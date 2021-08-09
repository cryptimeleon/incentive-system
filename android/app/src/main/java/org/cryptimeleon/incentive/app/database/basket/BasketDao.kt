package org.cryptimeleon.incentive.app.database.basket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface BasketDao {
    @Query("SELECT * FROM baskets WHERE active=1 LIMIT 1")
    fun getBasket(): Basket

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBasket(basket: Basket)

    @Query("UPDATE baskets SET active=:active WHERE basketId = :id")
    fun setActive(active: Boolean, id: UUID)

    @Query("UPDATE baskets SET active=0")
    fun setAllInactive()
}
