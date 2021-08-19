package org.cryptimeleon.incentive.app.data.database.crypto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class SerializedCryptoToken(
    @ColumnInfo(name = "serialized_token") val serializedToken: String,
    @ColumnInfo(name = "crypto_material_id") val cryptoMaterialId: Int,
    @PrimaryKey(autoGenerate = true) val promotionId: Int = 1,
)
