package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.UpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UserPromotionState
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.model.Token

/**
 * Use case that returns a flow of all promotion states.
 * A promotion state contains all relevant data and user state concerning a promotion which can be
 * further reduced to the desired set of information.
 */
class GetPromotionStatesUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository
) {

    operator fun invoke(): Flow<List<UserPromotionState>> =
        combine(
            promotionRepository.promotions,
            cryptoRepository.tokens,
            basketRepository.basket
        ) { promotions, tokens, basket ->
            promotions.map {
                val token =
                    tokens.find { token: Token -> token.promotionId == it.promotionParameters.promotionId }
                        ?: throw RuntimeException("No token for promotion found!")
                val basketPoints = it.computeEarningsForBasket(basket.toPromotionBasket())
                val tokenPoints = token.toBigIntVector()
                val updates = mutableListOf<UpdateChoice>(UpdateChoice.None)
                val metadata = it.generateMetadataForUpdate()
                if (it.fastEarnSupported && basketPoints.stream().anyMatch { x -> x.toInt() > 0 }) {
                    // Basket points must be non-zero (and positive) for a useful update
                    updates.add(UpdateChoice.Earn(basketPoints))
                }
                updates.addAll(
                    it.computeTokenUpdatesForPoints(tokenPoints, basketPoints, metadata)
                        .map { zkp ->
                            UpdateChoice.ZKP(
                                updateId = zkp.tokenUpdateId,
                                updateDescription = zkp.rewardDescription,
                                oldPoints = tokenPoints,
                                newPoints = zkp.computeSatisfyingNewPointsVector(
                                    tokenPoints,
                                    basketPoints,
                                    metadata
                                ).get(),
                                metadata = metadata,
                                sideEffect = zkp.sideEffect
                            )
                        })
                return@map UserPromotionState(
                    it.promotionParameters.promotionId,
                    it.promotionName,
                    basketPoints,
                    tokenPoints,
                    updates
                )
            }
        }
}
