package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.SerializableUserChoice
import org.cryptimeleon.incentive.promotion.Promotion
import java.math.BigInteger

class FakePromotionRepository(private val promotionList: List<Promotion>) : IPromotionRepository {

    private val _promotions: MutableStateFlow<List<Promotion>> = MutableStateFlow(emptyList())
    override val promotions: Flow<List<Promotion>>
        get() = _promotions
    override val userUpdateChoices: Flow<List<PromotionUserUpdateChoice>>
        get() = TODO("Not yet implemented")

    override suspend fun reloadPromotions() {
        _promotions.value = promotionList
    }

    override suspend fun putUserUpdateChoice(promotionId: BigInteger, choice: SerializableUserChoice.UserUpdateChoice) {
        TODO("Not yet implemented")
    }
}
