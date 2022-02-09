package org.cryptimeleon.incentive.app.data.database.promotion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey
    val promotionId: Int,
    val promotionRepresentation: String
)
