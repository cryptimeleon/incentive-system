package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.ZKP
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate
import org.cryptimeleon.incentive.promotion.sideeffect.NoSideEffect
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect
import org.cryptimeleon.incentive.promotion.streak.RangeProofStreakTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.SpendStreakTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.StandardStreakTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion
import org.cryptimeleon.incentive.promotion.vip.ProveVipTokenUpdate
import org.cryptimeleon.incentive.promotion.vip.UpgradeVipZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.vip.VipPromotion
import java.math.BigInteger
import java.util.*

/**
 * Use case that aggregates all information regarding the users state with promotions.
 */
class PromotionInfoUseCase(
    private val promotionRepository: IPromotionRepository,
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository
) {
    operator fun invoke(): Flow<List<PromotionData>> =
        combine(
            promotionRepository.promotions,
            promotionRepository.userUpdateChoices,
            cryptoRepository.tokens,
            basketRepository.rewardItems,
            basketRepository.basket
        ) { promotions: List<Promotion>,
            userUpdateChoices: List<PromotionUserUpdateChoice>,
            tokens: List<Token>,
            rewardItems: List<RewardItem>,
            basket: Basket?
            ->
            promotions.map { promotion ->
                val promotionInfoUseCaseWorker = PromotionInfoUseCaseWorker(
                    promotion, userUpdateChoices, tokens, rewardItems, basket
                )
                promotionInfoUseCaseWorker.computePromotionData()
            }
        }
}

private class PromotionInfoUseCaseWorker(
    val promotion: Promotion,
    userUpdateChoices: List<PromotionUserUpdateChoice>,
    tokens: List<Token>,
    val rewardItems: List<RewardItem>,
    basket: Basket?
) {
    private val promotionId = promotion.promotionParameters.promotionId
    private val token: Token = tokens.find { it.promotionId == promotionId }!!
    private val tokenPoints = token.toBigIntVector()
    private val basketPoints = promotion.computeEarningsForBasket(basket!!.toPromotionBasket())!!
    private val metadata = promotion.generateMetadataForUpdate()!!
    private val updateChoice = userUpdateChoices.find { it.promotionId == promotionId }

    private val feasibleTokenUpdates = computeFeasibleUpdates()
    private val zkpTokenUpdates: List<TokenUpdate> = computeZkpTokenUpdates()
    private val noUpdate: NoTokenUpdate =
        NoTokenUpdate(
            feasibility = computeNoTokenUpdateFeasibility(updateChoice)
        )

    private val earnUpdate: Optional<EarnTokenUpdate> = computeEarnUpdateOrEmptyIfDisabled()

    private val allTokenUpdates: List<TokenUpdate> =
        zkpTokenUpdates + listOf(noUpdate) + if (earnUpdate.isPresent) listOf(earnUpdate.get()) else emptyList()

    fun computePromotionData(): PromotionData {
        return when (promotion) {
            is HazelPromotion -> HazelPromotionData(promotion, token, allTokenUpdates)
            is VipPromotion -> VipPromotionData(promotion, token, allTokenUpdates)
            is StreakPromotion -> StreakPromotionData(promotion, token, allTokenUpdates)
            else -> {
                throw RuntimeException("Not implemented yet!")
            }
        }
    }

    private fun computeEarnUpdateOrEmptyIfDisabled(): Optional<EarnTokenUpdate> =
        if (!promotion.fastEarnSupported)
            Optional.empty()
        else if (basketPoints.stream().anyMatch { p: BigInteger -> p.signum() == 1 })
            Optional.of(
                EarnTokenUpdate(
                    feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                    "Your basket does not qualify for earning any points."
                )
            )
        else
            Optional.of(computeEarnUpdate(updateChoice))

    private fun computeEarnUpdate(updateChoice: PromotionUserUpdateChoice?): EarnTokenUpdate {
        val description = when (promotion) {
            is HazelPromotion -> "Collect ${basketPoints.get(0)} point" + if (basketPoints.get(0)
                    .toInt() > 1
            ) "s" else ""
            is VipPromotion -> "Collect ${basketPoints.get(0)} point" + if (basketPoints.get(0)
                    .toInt() > 1
            ) "s" else ""
            else -> {
                "Collect $basketPoints point"
            }
        }
        return EarnTokenUpdate(
            feasibility = if ((updateChoice != null) && (updateChoice.userUpdateChoice is Earn)) PromotionUpdateFeasibility.SELECTED else PromotionUpdateFeasibility.CANDIDATE,
            description = description,
        )
    }

    private fun computeZkpTokenUpdates(): List<TokenUpdate> =
        promotion.zkpTokenUpdates.map {
            val description = it.rewardDescription
            val rewardUpdateOrEmpty = when (val sideEffect = it.sideEffect!!) {
                is NoSideEffect -> Optional.empty<String>()
                is RewardSideEffect -> Optional.ofNullable(rewardItems.find { r -> r.id == sideEffect.rewardId }?.title)
                else -> {
                    throw RuntimeException("Side Effect $sideEffect not implemented yet!")
                }
            }
            val feasibility = computeZkpTokenUpdateFeasibility(it)
            when (it) {
                is HazelTokenUpdate -> HazelTokenUpdateState(
                    description,
                    rewardUpdateOrEmpty,
                    feasibility,
                    current = tokenPoints.get(0).toInt(),
                    goal = tokenPoints.get(0).toInt() + basketPoints.get(0).toInt()
                )
                is StandardStreakTokenUpdate -> StandardStreakTokenUpdateState(
                    description, rewardUpdateOrEmpty, feasibility
                )
                is SpendStreakTokenUpdate -> SpendStreakTokenUpdateState(
                    description,
                    rewardUpdateOrEmpty,
                    feasibility,
                    currentStreak = tokenPoints.get(0).toInt(),
                    requiredStreak = it.cost
                )
                is RangeProofStreakTokenUpdate -> RangeProofStreakTokenUpdateState(
                    description,
                    rewardUpdateOrEmpty,
                    feasibility,
                    currentStreak = tokenPoints.get(0).toInt(),
                    requiredStreak = it.lowerLimit
                )
                is UpgradeVipZkpTokenUpdate -> UpgradeVipTokenUpdateState(
                    description,
                    rewardUpdateOrEmpty,
                    feasibility,
                    tokenPoints.get(0).toInt(),
                    it.accumulatedCost,
                    VipStatus.fromInt(it.toVipStatus)
                )
                is ProveVipTokenUpdate -> ProveVipTokenUpdateState(
                    description,
                    rewardUpdateOrEmpty,
                    feasibility,
                    currentStatus = VipStatus.fromInt(tokenPoints.get(1).toInt()),
                    requiredStatus = VipStatus.fromInt(it.requiredStatus)
                )
                else -> {
                    throw RuntimeException("Not implemented yet")
                }
            }
        }

    private fun computeFeasibleUpdates(): List<ZkpTokenUpdate> =
        promotion.computeTokenUpdatesForPoints(tokenPoints, basketPoints, metadata)

    /**
     * One can always choose None, hence the feasibility is never NOT_APPLICABLE.
     */
    private fun computeNoTokenUpdateFeasibility(updateChoice: PromotionUserUpdateChoice?) =
        if ((updateChoice != null) && (updateChoice.userUpdateChoice is None)) PromotionUpdateFeasibility.SELECTED else PromotionUpdateFeasibility.CANDIDATE

    private fun computeZkpTokenUpdateFeasibility(zkpTokenUpdate: ZkpTokenUpdate): PromotionUpdateFeasibility {
        return if (feasibleTokenUpdates.any { it.tokenUpdateId == zkpTokenUpdate.tokenUpdateId }) {
            if (isSelectedUpdate(updateChoice, zkpTokenUpdate)) {
                PromotionUpdateFeasibility.SELECTED
            } else {
                PromotionUpdateFeasibility.CANDIDATE
            }
        } else {
            PromotionUpdateFeasibility.NOT_APPLICABLE
        }
    }

    private fun isSelectedUpdate(
        updateChoice: PromotionUserUpdateChoice?,
        zkpTokenUpdate: ZkpTokenUpdate
    ) =
        (updateChoice != null) && (updateChoice.userUpdateChoice is ZKP) && (updateChoice.userUpdateChoice.tokenUpdateId == zkpTokenUpdate.tokenUpdateId)

}