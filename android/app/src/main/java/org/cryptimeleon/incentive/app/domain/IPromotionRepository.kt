package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.promotion.promotions.Promotion

interface IPromotionRepository {
    val promotions: Flow<List<Promotion>>
    suspend fun reloadPromotions()
}