package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "baskets")
data class BasketEntity(
    @PrimaryKey val basketId: UUID,
    @ColumnInfo(name = "active") val isActive: Boolean
)

