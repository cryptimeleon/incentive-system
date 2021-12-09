package org.cryptimeleon.incentive.app.data.database.crypto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {
    @Query("SELECT * FROM crypto_material WHERE id LIKE 1")
    fun observeCryptoMaterial(): Flow<CryptoMaterialEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCryptoMaterial(cryptoMaterialEntity: CryptoMaterialEntity)

    @Query("SELECT * FROM tokens")
    fun observeTokens(): Flow<List<CryptoTokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(cryptoTokenEntity: CryptoTokenEntity)
}
