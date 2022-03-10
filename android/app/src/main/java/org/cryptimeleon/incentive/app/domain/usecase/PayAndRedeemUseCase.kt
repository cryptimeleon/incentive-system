package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.EarnRequestData
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.SpendRequestData
import org.cryptimeleon.incentive.app.domain.model.ZKP
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.math.serialization.converter.JSONConverter
import java.math.BigInteger
import java.util.*
import java.util.stream.Collectors

class PayAndRedeemUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository,
) {
    private val analyzeUserTokenUpdatesUseCase =
        AnalyzeUserTokenUpdatesUseCase(promotionRepository, cryptoRepository, basketRepository)

    operator fun invoke(): Flow<PayAndRedeemState> =
        flow {
            // This process will look as follows:
            // 0. Check user choices present for all promotions (or default to None?)
            // 1. Prepare all requests for Earn and ZKP (send as a batch?)
            // 2. Pay basket
            // 3. Obtain results to the Earn/ZKP requests
            // 4. Update tokens
            // 5. Clear basket and user choices
            emit(PayAndRedeemState.GEN_ZKP)
            // TODO some checks on the basket and user choices
            var userTokenUpdates: List<PromotionUserUpdateChoice> =
                analyzeUserTokenUpdatesUseCase().first()
            val basketId = basketRepository.basket.first()?.basketId
            if (basketId == null) {
                emit(PayAndRedeemState.ERROR)
                return@flow
            }
            val jsonConverter = JSONConverter()
            val basket = basketRepository.basket.first()!!
            val promotionParameters = promotionRepository.promotions.first()
            val cryptoMaterial = cryptoRepository.cryptoMaterial.first()!!
            val tokens = cryptoRepository.tokens.first()
            val incentiveSystem = IncentiveSystem(cryptoMaterial.pp)

            val earnUpdateRequests = userTokenUpdates.parallelStream().map {
                when (it.userUpdateChoice) {
                    Earn -> {
                        val earnRequest = incentiveSystem.generateEarnRequest(
                            tokens.find { token -> token.promotionId == it.promotionId },
                            cryptoMaterial.ppk,
                            cryptoMaterial.ukp
                        )
                        EarnRequestData(
                            it.promotionId,
                            jsonConverter.serialize(earnRequest.representation)
                        )
                    }
                    else -> null
                }
            }.filter { it != null }.collect(Collectors.toList())
            val spendUpdateRequests = userTokenUpdates.parallelStream().map {
                when (it.userUpdateChoice) {
                    is ZKP -> {
                        val token = tokens.find { token -> token.promotionId == it.promotionId }!!
                        val promotion =
                            promotionParameters.find { promotion -> promotion.promotionParameters.promotionId == it.promotionId }!!
                        val update =
                            promotion.zkpTokenUpdates.find { zkpTokenUpdate -> zkpTokenUpdate.tokenUpdateId == it.userUpdateChoice.tokenUpdateId }!!
                        val metadata = promotion.generateMetadataForUpdate()
                        val basketValue =
                            promotion.computeEarningsForBasket(basket.toPromotionBasket())
                        val newPointsVector = update.computeSatisfyingNewPointsVector(
                            token.toBigIntVector(),
                            basketValue,
                            metadata
                        ).get()
                        val zkpRequest = incentiveSystem.generateSpendRequest(
                            promotion.promotionParameters,
                            token,
                            cryptoMaterial.ppk,
                            newPointsVector,
                            cryptoMaterial.ukp,
                            basket.toPromotionBasket().getBasketId(cryptoMaterial.pp.bg.zn),
                            update.generateRelationTree(basketValue, metadata)
                        )

                        SpendRequestData(
                            it.promotionId,
                            update.tokenUpdateId,
                            jsonConverter.serialize(zkpRequest.representation),
                            jsonConverter.serialize(metadata.representation)
                        )
                    }
                    else -> null
                }
            }.filter { it != null }.collect(Collectors.toList())

            emit(PayAndRedeemState.SEND_REQUESTS)
            cryptoRepository.sendTokenUpdatesBatch(
                basketId, BulkRequestDto(
                    earnUpdateRequests as List<EarnRequestData>,
                    spendUpdateRequests as List<SpendRequestData>
                )
            )

            emit(PayAndRedeemState.PAY)
            basketRepository.payCurrentBasket()

            emit(PayAndRedeemState.RETRIEVE_RESPONSES)
            cryptoRepository.retrieveTokenUpdatesResults(basketId)

            // TODO update tokens
            emit(PayAndRedeemState.UPDATE_TOKENS)

            emit(PayAndRedeemState.FINISHED)
        }
}

enum class PayAndRedeemState {
    GEN_ZKP, SEND_REQUESTS, PAY, RETRIEVE_RESPONSES, UPDATE_TOKENS, FINISHED, ERROR
}
