package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.*
import java.util.*

@Entity(tableName = "baskets")
data class BasketEntity(
    @PrimaryKey val key: Int = 0, // Only store current basket :)
    val basketId: UUID,
    val paid: Boolean,
    val redeemed: Boolean,
    val value: Int,
)

// Only store the shopping items for current basket since room does not really support nested relationships
@Entity(tableName = "shopping-items")
data class BasketItemEntity(
    @PrimaryKey val shoppingItemId: String,
    val price: Int,
    val title: String,
    val count: Int
)
