package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping-items")
data class ShoppingItemEntity(
    @PrimaryKey val itemId: String,
    val price: Int,
    val title: String,
)