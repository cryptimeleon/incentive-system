package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = [BasketEntity::class, BasketItemEntity::class, ShoppingItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BasketDatabase : RoomDatabase() {
    abstract fun basketDatabaseDao(): BasketDao
}
