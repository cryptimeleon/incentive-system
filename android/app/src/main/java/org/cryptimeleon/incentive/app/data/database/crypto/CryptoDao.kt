package org.cryptimeleon.incentive.app.data.database.crypto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {
    // TODO to handle more than one promotions, remove the LIKE 0 condition

    @Query("SELECT * FROM crypto_material WHERE id LIKE 1 LIMIT 1")
    fun observeSerializedCryptoMaterial(): Flow<SerializedCryptoMaterial?>

    @Query("SELECT * FROM crypto_material WHERE id LIKE 1 LIMIT 1")
    fun observeCryptoMaterial(): Flow<SerializedCryptoMaterial?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAsset(serializedCryptoMaterial: SerializedCryptoMaterial?)

    @Query("SELECT * FROM tokens WHERE crypto_material_id LIKE 1 LIMIT 1")
    fun observeToken(): Flow<SerializedCryptoToken?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(serializedCryptoToken: SerializedCryptoToken)

    @Query("DELETE FROM tokens")
    fun deleteAllTokens()
}
