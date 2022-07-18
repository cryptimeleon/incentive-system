package org.cryptimeleon.incentive.app.domain.usecase

import org.cryptimeleon.incentive.app.domain.usecase.StreakDate.Companion.toLong
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion
import org.cryptimeleon.incentive.promotion.streak.StreakTokenUpdateTimestamp
import org.cryptimeleon.incentive.promotion.vip.VipPromotion
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

/*
 * Data classes for representing the apps state of promotions.
 * Represent which updates users have chosen, which are possible, etc.
 */

interface PromotionData {
    val tokenUpdates: List<TokenUpdate>
    val promotionImageUrl: String
    val pid: BigInteger
    val promotionName: String
    val promotionDescription: String
    val points: Vector<BigInteger>
}

enum class PromotionUpdateFeasibility {
    SELECTED, CANDIDATE, NOT_APPLICABLE
}

data class HazelPromotionData(
    override val promotionName: String,
    override val pid: BigInteger,
    override val promotionDescription: String,
    override val points: Vector<BigInteger>,
    override val tokenUpdates: List<TokenUpdate>,
) : PromotionData {

    constructor(
        promotion: HazelPromotion,
        token: Token,
        tokenUpdates: List<TokenUpdate>
    ) : this(
        promotionName = promotion.promotionName,
        pid = promotion.promotionParameters.promotionId,
        promotionDescription = promotion.promotionName,
        points = token.toBigIntVector(),
        tokenUpdates = tokenUpdates
    )

    override val promotionImageUrl: String
        get() =
            if (promotionName.contains("Nutella", ignoreCase = true)) {
                "https://cdn.pixabay.com/photo/2022/01/16/09/58/chocolate-spread-6941622_960_720.jpg"
            } else {
                "https://cdn.pixabay.com/photo/2019/06/16/21/48/cups-4278774_960_720.jpg"
            }
    val score = points.get(0).toInt()
}

data class StreakPromotionData(
    override val promotionName: String,
    override val pid: BigInteger,
    override val promotionDescription: String,
    override val tokenUpdates: List<TokenUpdate>,
    val streakInterval: Int,
    val streakCount: Int,
    val lastDate: StreakDate,
) : PromotionData {

    constructor(
        promotion: StreakPromotion,
        token: Token,
        tokenUpdates: List<TokenUpdate>
    ) : this(
        promotionName = promotion.promotionName,
        pid = promotion.promotionParameters.promotionId,
        promotionDescription = promotion.promotionName,
        streakCount = token.toBigIntVector().get(0).toInt(),
        lastDate = StreakDate.fromLong(token.toBigIntVector().get(1).toLong()),
        tokenUpdates = tokenUpdates,
        streakInterval = promotion.interval
    )


    val lastEpochDay = lastDate.toLong()
    val todayEpochDay = StreakTokenUpdateTimestamp.now()!!.timestamp!!
    override val points: Vector<BigInteger> =
        Vector.of(
            BigInteger.valueOf(streakCount.toLong()),
            BigInteger.valueOf(lastEpochDay)
        )
    val streakStillValid = (todayEpochDay - lastEpochDay <= 7)

    override val promotionImageUrl: String
        get() = "https://cdn.pixabay.com/photo/2015/05/31/14/23/organizer-791939_960_720.jpg"
}

sealed class StreakDate {
    object NONE : StreakDate()
    data class DATE(val date: LocalDate) : StreakDate()

    companion object {
        val epochZero = LocalDate.ofEpochDay(0)

        fun fromLong(epochDay: Long): StreakDate =
            if (epochDay == 0L) {
                NONE
            } else {
                DATE(LocalDate.ofEpochDay(epochDay))
            }

        fun StreakDate.toLong(): Long =
            when (this) {
                is NONE -> 0
                is DATE -> ChronoUnit.DAYS.between(epochZero, this.date)
            }
    }
}


data class VipPromotionData(
    override val promotionName: String,
    override val pid: BigInteger,
    override val promotionDescription: String,
    override val tokenUpdates: List<TokenUpdate>,
    val score: Int,
    val vipLevel: VipStatus
) : PromotionData {

    constructor(
        promotion: VipPromotion,
        token: Token,
        tokenUpdates: List<TokenUpdate>
    ) : this(
        promotionName = promotion.promotionName,
        pid = promotion.promotionParameters.promotionId,
        promotionDescription = promotion.promotionName,
        tokenUpdates = tokenUpdates,
        score = token.toBigIntVector().get(0).toInt(),
        vipLevel = VipStatus.fromInt(token.toBigIntVector().get(1).toInt())
    )

    override val points: Vector<BigInteger> =
        Vector.of(score.toBigInteger(), vipLevel.statusValue.toBigInteger())
    override val promotionImageUrl: String
        get() = "https://cdn.pixabay.com/photo/2017/10/11/07/18/eat-2840156_960_720.jpg"
}


interface TokenUpdate {
    val description: String
    val feasibility: PromotionUpdateFeasibility
}

data class NoTokenUpdate(
    override val feasibility: PromotionUpdateFeasibility = PromotionUpdateFeasibility.CANDIDATE,
) : TokenUpdate {
    override val description: String = "Nothing"
}

data class EarnTokenUpdate(
    override val feasibility: PromotionUpdateFeasibility,
    override val description: String,
) : TokenUpdate

interface ZkpTokenUpdate : TokenUpdate {
    val sideEffect: Optional<String>
}

data class HazelTokenUpdateState(
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val current: Int,
    val goal: Int
) : ZkpTokenUpdate

data class ProveVipTokenUpdateState(
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val currentStatus: VipStatus,
    val requiredStatus: VipStatus
) : ZkpTokenUpdate

data class UpgradeVipTokenUpdateState(
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val currentPoints: Int,
    val requiredPoints: Int,
    val targetVipStatus: VipStatus,
) : ZkpTokenUpdate

data class StandardStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
) : ZkpTokenUpdate

data class RangeProofStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val requiredStreak: Int,
    val currentStreak: Int
) : ZkpTokenUpdate

data class SpendStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val requiredStreak: Int,
    val currentStreak: Int
) : ZkpTokenUpdate

enum class VipStatus(val statusValue: Int) {
    NONE(0), // do not delete this, will need it later!
    BRONZE(1),
    SILVER(2),
    GOLD(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.statusValue == value }
    }
}
