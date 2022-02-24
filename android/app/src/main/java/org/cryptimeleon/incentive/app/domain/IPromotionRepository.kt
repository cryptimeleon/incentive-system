package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UserUpdateChoice
import org.cryptimeleon.incentive.promotion.Promotion
import java.math.BigInteger
import java.util.*


interface IPromotionRepository {
    val promotions: Flow<List<Promotion>>
    val userUpdateChoices: Flow<List<PromotionUserUpdateChoice>>
    suspend fun reloadPromotions()
    suspend fun putUserUpdateChoice(promotionId: BigInteger, choice: UserUpdateChoice)
}
