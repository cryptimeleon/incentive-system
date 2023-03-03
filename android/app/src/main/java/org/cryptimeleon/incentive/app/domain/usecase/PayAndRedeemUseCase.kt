package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.first
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature
import org.cryptimeleon.incentive.app.domain.*
import org.cryptimeleon.incentive.app.domain.model.*
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.*
import org.cryptimeleon.incentive.promotion.ContextManager
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata
import org.cryptimeleon.math.serialization.RepresentableRepresentation
import org.cryptimeleon.math.serialization.converter.JSONConverter
import org.cryptimeleon.math.structures.cartesian.Vector
import timber.log.Timber
import java.math.BigInteger
import java.util.*

class PayAndRedeemUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository
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
    suspend operator fun invoke(): PayAndRedeemStatus {
        try {
            // Some setup
            val userTokenUpdates: List<PromotionUserUpdateChoice> =
                analyzeUserTokenUpdatesUseCase().first()
            val basket = basketRepository.basket.first()
            val promotionParameters = promotionRepository.promotions.first()
            val cryptoMaterial = cryptoRepository.cryptoMaterial.first()!!
            val tokens = cryptoRepository.tokens.first()
            val pp = cryptoMaterial.pp
            val incentiveSystem = IncentiveSystem(pp)
            // Push basket to current store
            val basketId = basketRepository.pushCurrentBasket()

            // TODO some checks on the basket and user choices

            if (userTokenUpdates.all { it.userUpdateChoice is None }) {
                Timber.i("No token updates selected, just pay.")
                basketRepository.payBasket(basketId)
                basketRepository.discardCurrentBasket()
                return PayAndRedeemStatus.Success(basketId)
            }

            val earnUpdateRequestPairs: List<Pair<EarnRequestStoreDto, EarnStoreCache>> =
                generateEarnRequest(userTokenUpdates, tokens, incentiveSystem, cryptoMaterial)
            val spendUpdateRequestPairs: List<Pair<SpendRequestStoreDto, SpendStoreCache>> =
                generateSpendRequests(
                    userTokenUpdates,
                    tokens,
                    promotionParameters,
                    basket,
                    incentiveSystem,
                    cryptoMaterial,
                    basketId
                )
            cryptoRepository.sendTokenUpdatesBatchToStore(
                BulkRequestStoreDto(
                    basketId,
                    earnUpdateRequestPairs.map { it.first },
                    spendUpdateRequestPairs.map { it.first }
                )
            )

            basketRepository.payBasket(basketId)

            val updateResults = cryptoRepository.retrieveTokenUpdatesBatchStoreResults(basketId)
            val earnProviderRequests: List<Pair<EarnRequestProviderDto, EarnProviderCache>> =
                generateProviderEarnRequests(
                    updateResults,
                    promotionParameters,
                    earnUpdateRequestPairs,
                    basket,
                    basketId,
                    incentiveSystem,
                    cryptoMaterial
                )

            val spendProviderRequests: List<Pair<SpendRequestProviderDto, SpendProviderCache>> =
                generateProviderSpendRequests(
                    updateResults,
                    promotionParameters,
                    spendUpdateRequestPairs,
                    basketId
                )

            val bulkRequestProviderDto = BulkRequestProviderDto(
                spendProviderRequests.map { it.first },
                earnProviderRequests.map { it.first }
            )

            val bulkResultsProviderDto: BulkResultsProviderDto =
                cryptoRepository.sendTokenUpdatesBatchToProvider(bulkRequestProviderDto)
            computeTokenAfterEarn(
                bulkResultsProviderDto.earnResults,
                pp,
                earnProviderRequests,
                incentiveSystem,
                cryptoMaterial
            )

            computeTokensFromSpend(
                bulkResultsProviderDto.spendResults,
                spendProviderRequests,
                pp,
                incentiveSystem,
                cryptoMaterial
            )

            basketRepository.discardCurrentBasket()
            Timber.i("Finished Pay and Redeem")
            return PayAndRedeemStatus.Success(basketId)
        } catch (e: DSException) {
            Timber.e(e)
            return PayAndRedeemStatus.DSDetected
        } catch (e: PayRedeemException) {
            Timber.e(e)
            return PayAndRedeemStatus.Error(e)
        } catch (e: Exception) {
            Timber.e(e)
            return PayAndRedeemStatus.Error(e)
        }
    }

    private suspend fun computeTokensFromSpend(
        spendResultsProviderDtoList: List<SpendResultProviderDto>,
        spendProviderRequests: List<Pair<SpendRequestProviderDto, SpendProviderCache>>,
        pp: IncentivePublicParameters,
        incentiveSystem: IncentiveSystem,
        cryptoMaterial: CryptoMaterial
    ) {
        spendResultsProviderDtoList.forEach {
            val cache =
                spendProviderRequests.first { pair -> pair.second.promotionId == it.promotionId }.second
            val spendResponse =
                SpendProviderResponse(
                    jsonConverter.deserialize(
                        it.serializedSpendResult
                    ), pp
                )
            val token = incentiveSystem.retrieveUpdatedTokenFromSpendResponse(
                cryptoMaterial.ukp,
                cryptoMaterial.ppk,
                cache.token,
                cache.promotion.promotionParameters,
                cache.newPointsVector,
                cache.spendRequest,
                spendResponse
            )
            cryptoRepository.putToken(cache.promotion.promotionParameters, token)
        }
    }

    private suspend fun computeTokenAfterEarn(
        earnResultProviderDtoList: List<EarnResultProviderDto>,
        pp: IncentivePublicParameters,
        earnProviderRequests: List<Pair<EarnRequestProviderDto, EarnProviderCache>>,
        incentiveSystem: IncentiveSystem,
        cryptoMaterial: CryptoMaterial
    ) {
        earnResultProviderDtoList.forEach {
            val signature = SPSEQSignature(
                jsonConverter.deserialize(it.serializedEarnResponse),
                pp.bg.g1,
                pp.bg.g2
            )
            val cache =
                earnProviderRequests.first { pair -> pair.second.promotionId == it.promotionId }.second
            val token = incentiveSystem.handleEarnResponse(
                cache.earnProviderRequest,
                signature,
                cache.promotion.promotionParameters,
                cache.token,
                cryptoMaterial.ukp,
                cryptoMaterial.ppk
            )
            cryptoRepository.putToken(cache.promotion.promotionParameters, token)
        }
    }

    private fun generateProviderSpendRequests(
        updateResults: BulkResultStoreDto,
        promotionParameters: List<Promotion>,
        spendUpdateRequestPairs: List<Pair<SpendRequestStoreDto, SpendStoreCache>>,
        basketId: UUID
    ) = updateResults.spendResults.map {
        val promotion =
            promotionParameters.find { promotion -> promotion.promotionParameters.promotionId == it.promotionId }!!
        Timber.i("Spend for promotion ${promotion.promotionParameters.promotionId}")

        val cache =
            spendUpdateRequestPairs.find { pair -> pair.second.promotionId == it.promotionId }!!.second

        val spendStoreResponse =
            SpendStoreResponse(jsonConverter.deserialize(it.serializedSpendCouponSignature))
        val spendProviderRequest = SpendProviderRequest(
            cache.zkpRequest,
            spendStoreResponse
        )
        Pair(
            SpendRequestProviderDto(
                promotion.promotionParameters.promotionId,
                jsonConverter.serialize(spendProviderRequest.representation),
                jsonConverter.serialize(RepresentableRepresentation(cache.metadata)),
                basketId,
                cache.tokenUpdateId,
                cache.basketValue.toList()
            ),
            SpendProviderCache(
                promotion.promotionParameters.promotionId,
                promotion,
                cache.token,
                cache.newPointsVector,
                spendProviderRequest
            )
        )
    }

    private fun generateProviderEarnRequests(
        updateResults: BulkResultStoreDto,
        promotionParameters: List<Promotion>,
        earnUpdateRequestPairs: List<Pair<EarnRequestStoreDto, EarnStoreCache>>,
        basket: Basket,
        basketId: UUID,
        incentiveSystem: IncentiveSystem,
        cryptoMaterial: CryptoMaterial
    ) = updateResults.earnResults.map {
        val promotion =
            promotionParameters.find { promotion -> promotion.promotionParameters.promotionId == it.promotionId }!!
        Timber.i("Earn for promotion ${promotion.promotionParameters.promotionId}")
        val cache: EarnStoreCache =
            earnUpdateRequestPairs.find { pair -> pair.second.promotionId == it.promotionId }!!.second
        val earnAmount = promotion.computeEarningsForBasket(basket.toPromotionBasket(basketId))

        val earnStoreResponse =
            EarnStoreResponse(jsonConverter.deserialize(it.serializedEarnCouponSignature))
        val couponValid = incentiveSystem.verifyEarnCoupon(
            cache.earnRequest,
            promotion.promotionParameters.promotionId,
            earnAmount,
            earnStoreResponse,
        ) { true }

        if (!couponValid) {
            throw PayRedeemException(
                0,
                "Invalid coupon for promotion wiht id ${promotion.promotionParameters.promotionId}"
            )
        }

        val earnStoreRequest = incentiveSystem.generateEarnRequest(
            cache.token,
            cryptoMaterial.ppk,
            cryptoMaterial.ukp,
            earnAmount,
            earnStoreResponse
        )
        Pair(
            EarnRequestProviderDto(
                promotion.promotionParameters.promotionId,
                jsonConverter.serialize(earnStoreRequest.representation)
            ),
            EarnProviderCache(
                promotion.promotionParameters.promotionId,
                promotion,
                earnStoreRequest,
                cache.token
            )
        )
    }

    private fun generateSpendRequests(
        userTokenUpdates: List<PromotionUserUpdateChoice>,
        tokens: List<Token>,
        promotionParameters: List<Promotion>,
        basket: Basket,
        incentiveSystem: IncentiveSystem,
        cryptoMaterial: CryptoMaterial,
        basketId: UUID
    ) = userTokenUpdates.mapNotNull {
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
                    promotion.computeEarningsForBasket(basket.toPromotionBasket(basketId))
                val newPointsVector = update.computeSatisfyingNewPointsVector(
                    token.toBigIntVector(),
                    basketValue,
                    metadata
                ).get()
                val context =
                    ContextManager.computeContext(update.tokenUpdateId, basketValue, metadata)
                val zkpRequest = incentiveSystem.generateStoreSpendRequest(
                    cryptoMaterial.ukp,
                    cryptoMaterial.ppk,
                    token,
                    promotion.promotionParameters,
                    basketId,
                    newPointsVector,
                    update.generateRelationTree(basketValue, metadata),
                    context
                )

                Pair(
                    SpendRequestStoreDto(
                        jsonConverter.serialize(zkpRequest.representation),
                        it.promotionId,
                        update.tokenUpdateId,
                        jsonConverter.serialize(RepresentableRepresentation(metadata))
                    ),
                    SpendStoreCache(
                        promotion.promotionParameters.promotionId,
                        update.tokenUpdateId,
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

    private fun generateEarnRequest(
        userTokenUpdates: List<PromotionUserUpdateChoice>,
        tokens: List<Token>,
        incentiveSystem: IncentiveSystem,
        cryptoMaterial: CryptoMaterial
    ) = userTokenUpdates.mapNotNull {
        when (it.userUpdateChoice) {
            Earn -> {
                val token =
                    tokens.find { token -> token.promotionId == it.promotionId }!!
                val earnRequest = incentiveSystem.generateEarnCouponRequest(
                    token,
                    cryptoMaterial.ukp
                )
                Pair(
                    EarnRequestStoreDto(
                        it.promotionId,
                        jsonConverter.serialize(earnRequest.representation)
                    ),
                    EarnStoreCache(it.promotionId, earnRequest, token)
                )
            }
            else -> null
        }
    }
}


// Some data we need to store between request and response
data class EarnStoreCache(
    val promotionId: BigInteger,
    val earnRequest: EarnStoreRequest,
    val token: Token
)

data class SpendStoreCache(
    val promotionId: BigInteger,
    val tokenUpdateId: UUID,
    val zkpRequest: SpendStoreRequest,
    val basketValue: Vector<BigInteger>,
    val metadata: ZkpTokenUpdateMetadata,
    val newPointsVector: Vector<BigInteger>,
    val token: Token
)

data class EarnProviderCache(
    val promotionId: BigInteger,
    val promotion: Promotion,
    val earnProviderRequest: EarnProviderRequest,
    val token: Token
)

data class SpendProviderCache(
    val promotionId: BigInteger,
    val promotion: Promotion,
    val token: Token,
    val newPointsVector: Vector<BigInteger>,
    val spendRequest: SpendProviderRequest
)

sealed class PayAndRedeemStatus {
    data class Success(val basketId: UUID) : PayAndRedeemStatus()
    object DSDetected : PayAndRedeemStatus()
    data class Error(val e: Exception) : PayAndRedeemStatus()
}
