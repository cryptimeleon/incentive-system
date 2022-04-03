package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UpdateChoice
import org.cryptimeleon.incentive.app.domain.model.ZKP

/**
 * Filters the promotion updates chosen by users by removing those that are outdated since e.g. basket contents or metadata have changed.
 */
class AnalyzeUserTokenUpdatesUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository
) {

    operator fun invoke(): Flow<List<PromotionUserUpdateChoice>> =
        combine(
            promotionRepository.userUpdateChoices,
            GetPromotionStatesUseCase(
                promotionRepository,
                cryptoRepository,
                basketRepository
            ).invoke()
        ) { updates, promotionStates ->
            updates.filter { update ->
                promotionStates.any { state ->
                    state.promotionId.equals(update.promotionId) &&
                            when (update.userUpdateChoice) {
                                None -> true
                                Earn -> state.basketPoints.stream().anyMatch { it.toInt() > 0 }
                                is ZKP -> state.qualifiedUpdates.any {
                                    it is UpdateChoice.ZKP && it.updateId == update.userUpdateChoice.tokenUpdateId
                                }
                            }
                }
            }
        }
}
