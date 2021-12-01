package org.cryptimeleon.incentive.app.data;

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.cryptimeleon.incentive.app.data.network.PromotionApiService
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.promotions.Promotion

class PromotionRepository(
    private val promotionApiService: PromotionApiService,
) : IPromotionRepository {

    override val promotions: Flow<List<Promotion>> = flow {
        // TODO this can later emit automatic updates from the database
        val promotions = promotionApiService.getPromotions()
        if (promotions.isSuccessful) {
            emit(promotions.body()!!)
        }
    }


}
