package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID

@Dao
interface BasketDao {
    @Query("SELECT basketId FROM baskets WHERE active=1 LIMIT 1")
    suspend fun getActiveBasketId(): UUID?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBasket(basketEntity: BasketEntity)

    @Query("UPDATE baskets SET active=:active WHERE basketId = :id")
    fun setActive(active: Boolean, id: UUID)

    @Query("UPDATE baskets SET active=0")
    fun setAllInactive()
}
