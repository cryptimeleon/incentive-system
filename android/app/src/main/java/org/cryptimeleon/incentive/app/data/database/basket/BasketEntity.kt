package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "baskets")
data class BasketEntity(
    @PrimaryKey val key: Int = 0, // Only store current basket :)
    val basketId: UUID,
    val paid: Boolean,
)
