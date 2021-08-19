package org.cryptimeleon.incentive.app.data.database.crypto

import androidx.room.Database
import androidx.room.RoomDatabase

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = [SerializedCryptoToken::class, SerializedCryptoMaterial::class],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun cryptoDatabaseDao(): CryptoDao
}