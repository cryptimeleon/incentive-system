package org.cryptimeleon.incentive.app.domain.usecase

import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion
import org.cryptimeleon.incentive.promotion.streak.StreakTokenUpdateTimestamp
import org.cryptimeleon.incentive.promotion.vip.VipPromotion
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.time.LocalDate

interface PromotionData {
    val promotion: Promotion
    val token: Token
    val tokenUpdates: List<TokenUpdate>
    val promotionImageUrl: String
    val pid: BigInteger
        get() = promotion.promotionParameters.promotionId
    val promotionName: String
        get() = promotion.promotionName
    val promotionDescription: String
        get() = promotion.promotionDescription
    val points: Vector<BigInteger>
        get() = token.toBigIntVector()
}

enum class PromotionUpdateFeasibility {
    SELECTED, CANDIDATE, NOT_APPLICABLE
}

data class HazelPromotionData(
    override val promotion: HazelPromotion,
    override val token: Token,
    override val tokenUpdates: List<TokenUpdate>,
) : PromotionData {
    override val promotionImageUrl: String
        get() =
            if (promotion.promotionName.contains("Nutella")) {
                "url1"
            } else {
                "url2"
            }
    val score = points.get(0).toInt()
}

data class StreakPromotionData(
    override val promotion: StreakPromotion,
    override val token: Token,
    override val tokenUpdates: List<TokenUpdate>,
) : PromotionData {
    override val promotionImageUrl: String
        get() = "url"
    val streakCount = points.get(0).toInt()
    val streakInterval = promotion.interval!!
    val lastEpochDay = points.get(1).toLong()
    val lastDate = StreakDate.fromLong(lastEpochDay)
    val todayEpochDay = StreakTokenUpdateTimestamp.now()!!.timestamp!!
    val streakStillValid = (todayEpochDay - lastEpochDay <= 7)
}

sealed class StreakDate {
    object NONE
    data class DATE(val date: LocalDate)

    companion object {
        fun fromLong(epochDay: Long) {
            if (epochDay == 0L) {
                StreakDate.NONE
            } else {
                StreakDate.DATE(LocalDate.ofEpochDay(epochDay))
            }
        }
    }
}


data class VipPromotionData(
    override val promotion: VipPromotion,
    override val token: Token,
    override val tokenUpdates: List<TokenUpdate>,
) : PromotionData {
    override val promotionImageUrl: String
        get() = ""
    val score = points.get(0).toInt()
    val vipLevel = VipStatus.fromInt(points.get(1).toInt())
}


interface TokenUpdate {
    val description: String
    val feasibility: PromotionUpdateFeasibility
}

data class None(
    override val feasibility: PromotionUpdateFeasibility = PromotionUpdateFeasibility.CANDIDATE,
) : TokenUpdate {
    override val description: String = "Nothing"
}

data class Earn(
    override val feasibility: PromotionUpdateFeasibility
) : TokenUpdate {
    override val description: String
        get() = ""
}

interface ZKPUpdate: TokenUpdate {
    val sideEffect: String
}

data class HazelTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    override val feasibility: PromotionUpdateFeasibility,
    val current: Int,
    val goal: Int
) : ZKPUpdate

data class ProveVipTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    override val feasibility: PromotionUpdateFeasibility,
    val currentStatus: VipStatus,
    val requiredStatus: VipStatus
) : ZKPUpdate

data class UpgradeVipTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    override val feasibility: PromotionUpdateFeasibility,
    val currentPoints: Int,
    val requiredPoints: Int,
    val targetVipStatus: VipStatus,
) : ZKPUpdate

data class StandardStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    override val feasibility: PromotionUpdateFeasibility,
) : ZKPUpdate

data class RangeProofStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    override val feasibility: PromotionUpdateFeasibility,
    val requiredStreak: Int,
    val currentStreak: Int
) : ZKPUpdate

data class SpendStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    override val feasibility: PromotionUpdateFeasibility,
    val requiredStreak: Int,
    val currentStreak: Int
) : ZKPUpdate

enum class VipStatus(val statusValue: Int) {
    NONE(0), // do not delete this!
    BRONZE(1),
    SILVER(2),
    GOLD(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.statusValue == value }
    }
}
