package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Entity
import androidx.room.PrimaryKey

// Only store the shopping items for current basket since room does not really support nested relationships
@Entity(tableName = "basket-items")
data class BasketItemEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val price: Int,
    val count: Int
)