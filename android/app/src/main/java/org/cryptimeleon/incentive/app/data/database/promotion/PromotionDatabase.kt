package org.cryptimeleon.incentive.app.data.database.promotion

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PromotionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PromotionDatabase : RoomDatabase() {
    abstract fun promotionDatabaseDao(): PromotionDao
}