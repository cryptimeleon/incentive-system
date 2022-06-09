package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward-items")
data class RewardItemEntity(
    @PrimaryKey
    val id: String,
    val title: String
)
