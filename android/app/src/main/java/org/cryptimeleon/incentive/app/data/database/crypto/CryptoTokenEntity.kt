package org.cryptimeleon.incentive.app.data.database.crypto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class CryptoTokenEntity(
    @PrimaryKey val promotionId: Int,
    @ColumnInfo(name = "serialized_token") val serializedToken: String,
)
