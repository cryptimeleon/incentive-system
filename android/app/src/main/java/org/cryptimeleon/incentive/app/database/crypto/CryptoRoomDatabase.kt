package org.cryptimeleon.incentive.app.database.crypto

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = [Token::class, SerializedCryptoAsset::class],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun cryptoDatabaseDao(): CryptoDao

    companion object {

        @Volatile
        private var INSTANCE: CryptoDatabase? = null

        fun getInstance(context: Context): CryptoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CryptoDatabase::class.java, "crypto.db"
            ).build()
    }
}