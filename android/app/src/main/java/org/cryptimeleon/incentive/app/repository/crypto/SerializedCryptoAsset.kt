package org.cryptimeleon.incentive.app.repository.crypto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_assets")
data class SerializedCryptoAsset(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "serializedAsset") val serializedAsset: String
)