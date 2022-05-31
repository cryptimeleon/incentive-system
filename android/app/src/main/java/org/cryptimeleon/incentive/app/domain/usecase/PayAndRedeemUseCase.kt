package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature
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
import org.cryptimeleon.incentive.crypto.model.EarnRequest
import org.cryptimeleon.incentive.crypto.model.SpendRequest
import org.cryptimeleon.incentive.crypto.model.SpendResponse
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata
import org.cryptimeleon.math.serialization.RepresentableRepresentation
import org.cryptimeleon.math.serialization.converter.JSONConverter
import org.cryptimeleon.math.structures.cartesian.Vector
import timber.log.Timber
import java.math.BigInteger

class PayAndRedeemUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository,
) {
    private val jsonConverter = JSONConverter()
    private val analyzeUserTokenUpdatesUseCase =
        AnalyzeUserTokenUpdatesUseCase(promotionRepository, cryptoRepository, basketRepository)

    /**
     * Send all earn and spend requests for a basket, pay and retrieve updated tokens
     *
     * This process will look as follows:
     * 0. Check user choices present for all promotions (or default to None?)
     * 1. Prepare all requests for Earn and ZKP (send as a batch?)
     * 2. Pay basket
     * 3. Obtain results to the Earn/ZKP requests
     * 4. Update tokens
     * 5. Clear basket and user choices
     */
    suspend operator fun invoke(): Flow<PayAndRedeemState> =
        flow {
            // Some setup
            val userTokenUpdates: List<PromotionUserUpdateChoice> =
                analyzeUserTokenUpdatesUseCase().first()
            val basketId = basketRepository.basket.first()!!.basketId
            val basket = basketRepository.basket.first()!!
            val promotionParameters = promotionRepository.promotions.first()
            val cryptoMaterial = cryptoRepository.cryptoMaterial.first()!!
            val tokens = cryptoRepository.tokens.first()
            val incentiveSystem = IncentiveSystem(cryptoMaterial.pp)

            // TODO some checks on the basket and user choices

            emit(PayAndRedeemState.GEN_ZKP)
            Timber.i("Pay and redeem setup")
            Timber.i("Generate Earn Requests")
            val earnUpdateRequestPairs: List<Pair<EarnRequestData, EarnCache>> =
                userTokenUpdates.mapNotNull {
                    when (it.userUpdateChoice) {
                        Earn -> {
                            val token =
                                tokens.find { token -> token.promotionId == it.promotionId }!!
                            val earnRequest = incentiveSystem.generateEarnRequest(
                                token,
                                cryptoMaterial.ppk,
                                cryptoMaterial.ukp
                            )
                            Pair(
                                EarnRequestData(
                                    it.promotionId,
                                    jsonConverter.serialize(earnRequest.representation)
                                ),
                                EarnCache(it.promotionId, earnRequest, token)
                            )
                        }
                        else -> null
                    }
                }

            Timber.i("Generate Spend Requests")
            val spendUpdateRequestPairs: List<Pair<SpendRequestData, SpendCache>> =
                userTokenUpdates.mapNotNull {
                    when (it.userUpdateChoice) {
                        is ZKP -> {
                            val token =
                                tokens.find { token -> token.promotionId == it.promotionId }!!
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

                            Pair(
                                SpendRequestData(
                                    it.promotionId,
                                    update.tokenUpdateId,
                                    jsonConverter.serialize(zkpRequest.representation),
                                    jsonConverter.serialize(RepresentableRepresentation(metadata))
                                ),
                                SpendCache(
                                    promotion.promotionParameters.promotionId,
                                    zkpRequest,
                                    basketValue,
                                    metadata,
                                    newPointsVector,
                                    token
                                )
                            )
                        }
                        else -> null
                    }
                }

            Timber.i("Send requests")
            emit(PayAndRedeemState.SEND_REQUESTS)
            cryptoRepository.sendTokenUpdatesBatch(
                basketId, BulkRequestDto(
                    earnUpdateRequestPairs.map { it.first },
                    spendUpdateRequestPairs.map { it.first }
                )
            )

            Timber.i("Pay basket")
            emit(PayAndRedeemState.PAY)
            basketRepository.payCurrentBasket()


            Timber.i("Retrieve responses")
            emit(PayAndRedeemState.RETRIEVE_RESPONSES)
            val updateResults = cryptoRepository.retrieveTokenUpdatesResults(basketId)
            val pp = incentiveSystem.pp

            Timber.i("Update tokens")
            emit(PayAndRedeemState.UPDATE_TOKENS)

            Timber.i("Earn updates")
            updateResults.earnTokenUpdateResultDtoList.forEach { it ->
                val promotion =
                    promotionParameters.find { promotion -> promotion.promotionParameters.promotionId == it.promotionId }!!
                Timber.i("Earn for promotion ${promotion.promotionParameters.promotionId}")
                val cache: EarnCache =
                    earnUpdateRequestPairs.find { pair -> pair.second.promotionId == it.promotionId }!!.second
                val earnAmount = promotion.computeEarningsForBasket(basket.toPromotionBasket())

                val updatedToken = incentiveSystem.handleEarnRequestResponse(
                    promotion.promotionParameters,
                    cache.earnRequest,
                    SPSEQSignature(
                        jsonConverter.deserialize(it.serializedEarnResponse),
                        pp.bg.g1,
                        pp.bg.g2
                    ),
                    earnAmount,
                    cache.token,
                    cryptoMaterial.ppk,
                    cryptoMaterial.ukp
                )

                cryptoRepository.putToken(promotion.promotionParameters, updatedToken)
            }
            Timber.i("Finished all earn-updates")

            Timber.i("Spend updates")
            updateResults.zkpTokenUpdateResultDtoList.forEach {
                val promotion =
                    promotionParameters.find { promotion -> promotion.promotionParameters.promotionId == it.promotionId }!!
                Timber.i("Spend for promotion ${promotion.promotionParameters.promotionId}")

                val cache =
                    spendUpdateRequestPairs.find { pair -> pair.second.promotionId == it.promotionId }!!.second

                val updatedToken = incentiveSystem.handleSpendRequestResponse(
                    promotion.promotionParameters,
                    SpendResponse(
                        jsonConverter.deserialize(it.serializedResponse),
                        pp.bg.zn,
                        pp.spsEq
                    ),
                    cache.zkpRequest,
                    cache.token,
                    cache.newPointsVector,
                    cryptoMaterial.ppk,
                    cryptoMaterial.ukp
                )

                cryptoRepository.putToken(promotion.promotionParameters, updatedToken)
            }


            Timber.i("Delete basket")
            basketRepository.discardCurrentBasket(true)

            Timber.i("Finished Pay and Redeem")
            emit(PayAndRedeemState.FINISHED)
        }
}


// Some data we need to store between request and response
data class EarnCache(val promotionId: BigInteger, val earnRequest: EarnRequest, val token: Token)

data class SpendCache(
    val promotionId: BigInteger,
    val zkpRequest: SpendRequest,
    val basketValue: Vector<BigInteger>,
    val metadata: ZkpTokenUpdateMetadata,
    val newPointsVector: Vector<BigInteger>,
    val token: Token
)

enum class PayAndRedeemState {
    GEN_ZKP, SEND_REQUESTS, PAY, RETRIEVE_RESPONSES, UPDATE_TOKENS, FINISHED, ERROR, NOT_STARTED
}
