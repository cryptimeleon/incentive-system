package org.cryptimeleon.incentive.app.data;

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionDao
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionEntity
import org.cryptimeleon.incentive.app.data.network.PromotionApiService
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion
import org.cryptimeleon.incentive.promotion.promotions.Promotion
import org.cryptimeleon.math.serialization.converter.JSONConverter

class PromotionRepository(
    private val promotionApiService: PromotionApiService,
    private val promotionDao: PromotionDao
) : IPromotionRepository {

    private val jsonConverter: JSONConverter = JSONConverter()

    override val promotions: Flow<List<Promotion>> =
        promotionDao.observePromotions().map { promotionList: List<PromotionEntity> ->
            promotionList.map { promotionEntity: PromotionEntity ->
                NutellaPromotion(jsonConverter.deserialize(promotionEntity.promotionRepresentation))
            }
        }

    override suspend fun reloadPromotions() {
        val promotionsResponse = promotionApiService.getPromotions()
        if (promotionsResponse.isSuccessful) {
            val promotionEntities = promotionsResponse.body()!!.map { promotionString ->
                val promotion = NutellaPromotion(jsonConverter.deserialize(promotionString))
                PromotionEntity(
                    promotion.promotionParameters.promotionId.toInt(),
                    promotionString
                )
            }
            promotionDao.insertPromotions(promotionEntities)
        } else {
            throw RuntimeException("Could not load promotions!")
        }
    }
}
