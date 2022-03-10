package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.PromotionState
import org.cryptimeleon.incentive.app.domain.model.UpdateChoice
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.model.Token

class GetPromotionStatesUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository
) {

    operator fun invoke(): Flow<List<PromotionState>> =
        combine(
            promotionRepository.promotions,
            cryptoRepository.tokens,
            basketRepository.basket
        ) { promotions, tokens, basket ->
            if (basket == null) return@combine emptyList<PromotionState>()

            return@combine promotions.map {
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
                                update = zkp,
                                oldPoints = tokenPoints,
                                newPoints = zkp.computeSatisfyingNewPointsVector(
                                    tokenPoints,
                                    basketPoints,
                                    metadata
                                ).get(),
                                metadata = metadata
                            )
                        })
                return@map PromotionState(it, basketPoints, tokenPoints, updates)
            }
        }
}
