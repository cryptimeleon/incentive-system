package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.promotions.Promotion

class FakePromotionRepository(private val promotionList: List<Promotion>) : IPromotionRepository {

    private val _promotions: MutableStateFlow<List<Promotion>> = MutableStateFlow(emptyList())
    override val promotions: Flow<List<Promotion>>
        get() = _promotions

    override suspend fun reloadPromotions() {
        _promotions.value = promotionList
    }
}
