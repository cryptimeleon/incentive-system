package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.SerializableUserChoice
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

class PromotionInfoUseCaseWorker(
    val promotion: Promotion,
    userUpdateChoices: List<PromotionUserUpdateChoice>,
    val tokens: List<Token>,
    val rewardItems: List<RewardItem>,
    val basket: Basket?
) {
    private val promotionId = promotion.promotionParameters.promotionId
    private val token: Token = tokens.find { it.promotionId == promotionId }!!
    private val tokenPoints = token.toBigIntVector()
    private val basketPoints = promotion.computeEarningsForBasket(basket!!.toPromotionBasket())!!
    private val metadata = promotion.generateMetadataForUpdate()!!
    private val updateChoice = userUpdateChoices.find { it.promotionId == promotionId }

    private val feasibleTokenUpdates = computeFeasibleUpdates()
    private val zkpTokenUpdates: List<TokenUpdate> = computeZkpTokenUpdates()
    private val noUpdate: None =
        None(
            feasibility = if (updateChoice != null && updateChoice.userUpdateChoice is SerializableUserChoice.None) PromotionUpdateFeasibility.SELECTED else PromotionUpdateFeasibility.CANDIDATE
        )
    private val earnUpdate: Earn? =
        if (promotion.fastEarnSupported && basketPoints.stream()
                .anyMatch { p: BigInteger -> p.signum() == 1 }
        ) buildEarnUpdate(updateChoice)
        else null

    private fun buildEarnUpdate(updateChoice: PromotionUserUpdateChoice?): Earn {
        val description = when (promotion) {
            is HazelPromotion -> "Collect ${basketPoints.get(0)} point" + if (basketPoints.get(0)
                    .toInt() > 1
            ) "s" else ""
            is VipPromotion -> "Collect ${basketPoints.get(0)} point" + if (basketPoints.get(0)
                    .toInt() > 1
            ) "s" else ""
            else -> {
                "Collect ${basketPoints} point"
            }
        }
        return Earn(
            feasibility = if (updateChoice != null && updateChoice.userUpdateChoice is SerializableUserChoice.Earn) PromotionUpdateFeasibility.SELECTED else PromotionUpdateFeasibility.CANDIDATE,
            description = description,
        )
    }

    private val allTokenUpdates: List<TokenUpdate> = listOfNotNull(
        noUpdate,
        earnUpdate
    ) + zkpTokenUpdates // Not null since some promotions do not support earn

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

    private fun computeZkpTokenUpdates(): List<TokenUpdate> =
        promotion.zkpTokenUpdates.map {
            val description = it.rewardDescription
            val sideEffect = when (val e = it.sideEffect!!) {
                is NoSideEffect -> Optional.empty<String>()
                is RewardSideEffect -> Optional.ofNullable(rewardItems.find { r -> r.id == e.rewardId }?.title)
                else -> {
                    throw RuntimeException("Side Effect $e not implemented yet!")
                }
            }
            val feasibility = getFeasibility(it)
            when (it) {
                is HazelTokenUpdate -> hazelTokenUpdateState(description, sideEffect, feasibility)
                is StandardStreakTokenUpdate -> StandardStreakTokenUpdateState(
                    description, sideEffect, feasibility
                )
                is SpendStreakTokenUpdate -> SpendStreakTokenUpdateState(
                    description,
                    sideEffect,
                    feasibility,
                    currentStreak = tokenPoints.get(0).toInt(),
                    requiredStreak = it.cost
                )
                is RangeProofStreakTokenUpdate -> RangeProofStreakTokenUpdateState(
                    description,
                    sideEffect,
                    feasibility,
                    currentStreak = tokenPoints.get(0).toInt(),
                    requiredStreak = it.lowerLimit
                )
                is UpgradeVipZkpTokenUpdate -> UpgradeVipTokenUpdateState(
                    description,
                    sideEffect,
                    feasibility,
                    tokenPoints.get(0).toInt(),
                    it.accumulatedCost,
                    VipStatus.fromInt(it.toVipStatus)
                )
                is ProveVipTokenUpdate -> ProveVipTokenUpdateState(
                    description,
                    sideEffect,
                    feasibility,
                    currentStatus = VipStatus.fromInt(tokenPoints.get(1).toInt()),
                    requiredStatus = VipStatus.fromInt(it.requiredStatus)
                )
                else -> {
                    throw RuntimeException("Not implemented yet")
                }
            }
        }

    private fun hazelTokenUpdateState(
        description: String,
        sideEffect: Optional<String>,
        feasibility: PromotionUpdateFeasibility
    ) = HazelTokenUpdateState(
        description,
        sideEffect,
        feasibility,
        current = tokenPoints.get(0).toInt(),
        goal = tokenPoints.get(0).toInt() + basketPoints.get(0).toInt()
    )

    private fun computeFeasibleUpdates(): List<ZkpTokenUpdate> =
        promotion.computeTokenUpdatesForPoints(tokenPoints, basketPoints, metadata)

    private fun getFeasibility(zkpTokenUpdate: ZkpTokenUpdate): PromotionUpdateFeasibility {
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
        updateChoice != null && updateChoice.userUpdateChoice is SerializableUserChoice.ZKP && updateChoice.userUpdateChoice.tokenUpdateId == zkpTokenUpdate.tokenUpdateId

}
