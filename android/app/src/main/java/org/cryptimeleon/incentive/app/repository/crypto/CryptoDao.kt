package org.cryptimeleon.incentive.app.repository.crypto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CryptoDao {
    @Query("SELECT * FROM tokens")
    fun getTokens(): List<Token>

    @Query("SELECT serializedAsset FROM crypto_assets where name LIKE :name LIMIT 1")
    fun getAssetByName(name: String): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: Token)

    @Query("DELETE FROM tokens")
    fun deleteAllTokens()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAsset(asset: SerializedCryptoAsset)
}
