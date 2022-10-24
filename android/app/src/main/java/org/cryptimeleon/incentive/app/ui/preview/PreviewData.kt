package org.cryptimeleon.incentive.app.ui.preview

import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.HazelPromotionData
import org.cryptimeleon.incentive.app.domain.usecase.HazelTokenUpdateState
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
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
                BasketItem("ITEM1", "Hazelnut Spread", 199, 3),
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

        val hazelPromotionData = HazelPromotionData(
            promotionName = "Hazelnut Spread Promotion",
            pid = BigInteger.valueOf(5345L),
            promotionDescription = "Earn points for buying Hazelnut Spread!",
            points = Vector.of(BigInteger.valueOf(6L)),
            tokenUpdates = listOf(
                NoTokenUpdate(),
                EarnTokenUpdate(
                    PromotionUpdateFeasibility.CANDIDATE,
                    "Earn 2 Points",
                    Vector.of(6),
                    Vector.of(2),
                    Vector.of(8),
                ),
                HazelTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Get a free glass of Hazelnut Spread",
                    sideEffect = Optional.of("Free Hazelnut Spread"),
                    feasibility = PromotionUpdateFeasibility.SELECTED,
                    current = 6,
                    goal = 4,
                    basketPoints = 3
                )
            ),
            tokenHash = "8458b17882973b01de083501c29579a6",
            tokenJson = "This is some json for a token"
        )

        val vipPromotionData = VipPromotionData(
            promotionName = "VIP Promotion",
            pid = BigInteger.valueOf(3453L),
            promotionDescription = "Become BRONZE, SIlVER or GOLD by collecting points!",
            tokenUpdates = listOf(
                NoTokenUpdate(feasibility = PromotionUpdateFeasibility.SELECTED),
                ProveVipTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Prove you are SILVER",
                    sideEffect = Optional.of("5% Discount"),
                    feasibility = PromotionUpdateFeasibility.CANDIDATE,
                    currentPoints = 250,
                    basketPoints = 20,
                    currentStatus = VipStatus.SILVER,
                    requiredStatus = VipStatus.SILVER,
                ),
                ProveVipTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Prove you are GOLD",
                    sideEffect = Optional.of("10% Discount"),
                    feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                    currentPoints = 250,
                    basketPoints = 20,
                    currentStatus = VipStatus.SILVER,
                    requiredStatus = VipStatus.GOLD
                ),
                UpgradeVipTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Become GOLD",
                    sideEffect = Optional.of("10% Discount"),
                    feasibility = PromotionUpdateFeasibility.NOT_APPLICABLE,
                    currentPoints = 250,
                    basketPoints = 20,
                    requiredPoints = 300,
                    targetVipStatus = VipStatus.GOLD,
                    currentVipStatus = VipStatus.SILVER
                )
            ),
            score = 250,
            vipLevel = VipStatus.SILVER,
            tokenHash = "65b753d8aa7c6cd1461fc70041a45412",
            tokenJson = "This is some json for a token",
            bronzeScore = 100,
            silverScore = 200,
            goldScore = 300,
        )

        val streakPromotionData = StreakPromotionData(
            promotionName = "Streak Promotion",
            pid = BigInteger.valueOf(3467L),
            promotionDescription = "Increase your streak by shopping within a week",
            tokenUpdates = listOf(
                NoTokenUpdate(),
                StandardStreakTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Update your streak",
                    sideEffect = Optional.empty(),
                    feasibility = PromotionUpdateFeasibility.SELECTED,
                    lastDate = StreakDate.DATE(LocalDate.of(2022, 8, 8)),
                    newLastDate = LocalDate.of(2022, 8, 12),
                    currentStreak = 3,
                    newCurrentStreak = 4,
                    intervalDays = 7
                ),
                RangeProofStreakTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Prove that streak is at least 5",
                    sideEffect = Optional.of("Free Coffee"),
                    feasibility = PromotionUpdateFeasibility.CANDIDATE,
                    requiredStreak = 10,
                    currentStreak = 11,
                    lastDate = StreakDate.DATE(LocalDate.of(2022, 8, 8)),
                    newLastDate = LocalDate.of(2022, 8, 12),
                    newCurrentStreak = 12,
                    intervalDays = 7
                ),
                SpendStreakTokenUpdateState(
                    zkpUpdateId = UUID.randomUUID(),
                    description = "Spend streak to get a reward",
                    sideEffect = Optional.of("Teddy Bear"),
                    feasibility = PromotionUpdateFeasibility.CANDIDATE,
                    requiredStreak = 3,
                    currentStreak = 3
                )
            ),
            streakInterval = 7,
            streakCount = 3,
            lastDate = StreakDate.DATE(LocalDate.now()),
            tokenHash = "6f032a98978f20268bc3b4ffe54b8d0e",
            tokenJson = "This is some json for a token"
        )

        val promotionDataList = listOf(
            hazelPromotionData,
            vipPromotionData,
            streakPromotionData,
        )
    }
}
