package org.cryptimeleon.incentive.app.ui.preview

import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionUpdateFeasibility
import org.cryptimeleon.incentive.app.domain.usecase.ProveVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.RangeProofStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.SpendStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StandardStreakTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.StreakDate
import org.cryptimeleon.incentive.app.domain.usecase.StreakPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.UpgradeVipTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.VipPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.VipStatus
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.time.LocalDate
import java.util.*

class PreviewData {

    companion object {
        val basket = Basket(
            basketId = UUID.randomUUID(),
            items = listOf(
                BasketItem("ITEM1", "Nutella", 199, 3),
                BasketItem("ITEM2", "Grapeseed Oil", 239, 1),
                BasketItem("ITEM3", "Apple", 59, 2),
            ),
            paid = false,
            redeemed = false,
            value = 3 * 199 + 1 * 238 + 2 * 59
        )

        val emptyBasket = Basket(
            basketId = UUID.randomUUID(),
            items = emptyList(),
            paid = false,
            redeemed = false,
            value = 0
        )

        val promotionDataList = listOf<PromotionData>(
            HazelPromotionData(
                promotionName = "Nutella Promotion",
                pid = BigInteger.valueOf(5345L),
                promotionDescription = "Earn points for buying Nutella!",
                points = Vector.of(BigInteger.valueOf(6L)),
                tokenUpdates = listOf(
                    NoTokenUpdate(),
                    EarnTokenUpdate(PromotionUpdateFeasibility.CANDIDATE, "Earn 2 Points"),
                    HazelTokenUpdateState(
                        description = "Get a free glass of Nutella",
                        sideEffect = Optional.of("Free Nutella"),
                        feasibility = PromotionUpdateFeasibility.SELECTED,
                        current = 6,
                        goal = 4
                    )
                )
            ),
            VipPromotionData(
                promotionName = "VIP Promotion",
                pid = BigInteger.valueOf(3453L),
                promotionDescription = "Become BRONZE, SIlVER or GOLD by collecting points!",
                tokenUpdates = listOf(
                    NoTokenUpdate(feasibility = PromotionUpdateFeasibility.SELECTED),
                    ProveVipTokenUpdateState(
                        description = "Prove you are SILVER",
                        sideEffect = Optional.of("5% Discount"),
                        feasibility = PromotionUpdateFeasibility.CANDIDATE,
                        currentStatus = VipStatus.SILVER,
                        requiredStatus = VipStatus.SILVER
                    ),
                    ProveVipTokenUpdateState(
                        description = "Prove you are GOLD",
                        sideEffect = Optional.of("10% Discount"),
                        feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                        currentStatus = VipStatus.SILVER,
                        requiredStatus = VipStatus.GOLD
                    ),
                    UpgradeVipTokenUpdateState(
                        description = "Become GOLD",
                        sideEffect = Optional.of("10% Discount"),
                        feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                        currentPoints = 250,
                        requiredPoints = 300,
                        targetVipStatus = VipStatus.GOLD
                    )
                ),
                score = 250,
                vipLevel = VipStatus.SILVER
            ),
            StreakPromotionData(
                promotionName = "Streak Promotion",
                pid = BigInteger.valueOf(3467L),
                promotionDescription = "Increase your streak by shopping within a week",
                tokenUpdates = listOf(
                    NoTokenUpdate(),
                    StandardStreakTokenUpdateState(
                        description = "Update your streak",
                        sideEffect = Optional.empty(),
                        feasibility = PromotionUpdateFeasibility.SELECTED
                    ),
                    RangeProofStreakTokenUpdateState(
                        description = "Prove that streak is at least 5",
                        sideEffect = Optional.of("Free Coffee"),
                        feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                        requiredStreak = 10,
                        currentStreak = 3
                    ),
                    SpendStreakTokenUpdateState(
                        description = "Spend streak to get a reward",
                        sideEffect = Optional.of("Teddy Bear"),
                        feasibility = PromotionUpdateFeasibility.CANDIDATE,
                        requiredStreak = 3,
                        currentStreak = 3
                    )
                ),
                streakInterval = 7,
                streakCount = 3,
                lastDate = StreakDate.DATE(LocalDate.now())
            )
        )
    }
}
