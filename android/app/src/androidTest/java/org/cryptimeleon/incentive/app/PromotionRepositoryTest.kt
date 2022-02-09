package org.cryptimeleon.incentive.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.cryptimeleon.incentive.app.data.BasePromotionRepositoryTest
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionDao
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionDatabase
import org.cryptimeleon.incentive.app.data.network.FakePromotionApiService

class PromotionRepositoryTest : BasePromotionRepositoryTest() {
    private lateinit var promotionDao: PromotionDao
    private lateinit var db: PromotionDatabase

    private val fakePromotionApiService = FakePromotionApiService(promotions)

    override fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PromotionDatabase::class.java).build()
        promotionDao = db.promotionDatabaseDao()
        promotionRepository = PromotionRepository(fakePromotionApiService, promotionDao)
    }

    override fun after() {
        db.close()
    }
}
