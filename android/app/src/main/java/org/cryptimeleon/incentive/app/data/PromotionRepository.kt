package org.cryptimeleon.incentive.app.data;

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionDao
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionEntity
import org.cryptimeleon.incentive.app.data.network.PromotionApiService
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.math.serialization.RepresentableRepresentation
import org.cryptimeleon.math.serialization.converter.JSONConverter

class PromotionRepository(
    private val promotionApiService: PromotionApiService,
    private val promotionDao: PromotionDao
) : IPromotionRepository {

    private val jsonConverter: JSONConverter = JSONConverter()

    override val promotions: Flow<List<Promotion>> =
        promotionDao.observePromotions().map { promotionList: List<PromotionEntity> ->
            promotionList.map { promotionEntity: PromotionEntity ->
                (jsonConverter.deserialize(promotionEntity.promotionRepresentation) as RepresentableRepresentation)
                    .recreateRepresentable() as Promotion
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun reloadPromotions() {
        val promotionsResponse = promotionApiService.getPromotions()
        if (promotionsResponse.isSuccessful) {
            val promotionEntities = promotionsResponse.body()!!.map { promotionString ->
                val promotion =
                    (jsonConverter.deserialize(promotionString) as RepresentableRepresentation).recreateRepresentable() as Promotion
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
