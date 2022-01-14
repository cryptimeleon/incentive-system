package org.cryptimeleon.incentive.app.data.database.crypto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_material")
data class CryptoMaterialEntity(
    @ColumnInfo(name = "public_parameters") val serializedPublicParameters: String,
    @ColumnInfo(name = "provider_public_key") val serializedProviderPublicKey: String,
    @ColumnInfo(name = "user_secret_key") val serializedUserPublicKey: String,
    @ColumnInfo(name = "user_public_key") val serializedUserSecretKey: String,
    @PrimaryKey val id: Int = 1,
)
