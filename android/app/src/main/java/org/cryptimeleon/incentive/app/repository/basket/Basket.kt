package org.cryptimeleon.incentive.app.repository.basket

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "baskets")
data class Basket(
    @PrimaryKey val basketId: UUID
)

