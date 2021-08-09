package org.cryptimeleon.incentive.app.database.basket

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.cryptimeleon.incentive.app.database.UUIDConverter

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = [Basket::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UUIDConverter::class)
abstract class BasketDatabase : RoomDatabase() {
    abstract fun basketDatabaseDao(): BasketDao

    companion object {

        @Volatile
        private var INSTANCE: BasketDatabase? = null

        fun getInstance(context: Context): BasketDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                BasketDatabase::class.java, "basket.db"
            ).build()
    }
}