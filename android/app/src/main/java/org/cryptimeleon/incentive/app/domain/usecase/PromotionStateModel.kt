package org.cryptimeleon.incentive.app.domain.usecase

import org.cryptimeleon.incentive.app.domain.usecase.StreakDate.Companion.toLong
import org.cryptimeleon.incentive.app.util.toBigIntVector
import org.cryptimeleon.incentive.crypto.TokenDsidHashMaker
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion
import org.cryptimeleon.incentive.promotion.streak.StreakTokenUpdateTimestamp
import org.cryptimeleon.incentive.promotion.vip.VipPromotion
import org.cryptimeleon.math.serialization.converter.JSONConverter
import org.cryptimeleon.math.structures.cartesian.Vector
import org.json.JSONObject
import timber.log.Timber
import java.math.BigInteger
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

private val jsonConverter = JSONConverter()

private fun tokenToJsonString(token: Token) =
    JSONObject(jsonConverter.serialize(token.representation)).toString(2)

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
    val tokenHash: String
    val shortTokenHash: String
        get() = TokenDsidHashMaker.shortHash(tokenHash)
    val tokenJson: String
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
    override val tokenHash: String,
    override val tokenJson: String
) : PromotionData {

    constructor(
        promotion: HazelPromotion,
        token: Token,
        tokenUpdates: List<TokenUpdate>, pp: IncentivePublicParameters

    ) : this(
        promotionName = promotion.promotionName,
        pid = promotion.promotionParameters.promotionId,
        promotionDescription = promotion.promotionDescription,
        points = token.toBigIntVector(),
        tokenUpdates = tokenUpdates,
        tokenHash = TokenDsidHashMaker.hashToken(token, pp),
        tokenJson = tokenToJsonString(token)
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
    override val tokenHash: String,
    override val tokenJson: String,
    val streakInterval: Int,
    val streakCount: Int,
    val lastDate: StreakDate,
) : PromotionData {

    constructor(
        promotion: StreakPromotion,
        token: Token,
        tokenUpdates: List<TokenUpdate>,
        pp: IncentivePublicParameters
    ) : this(
        promotionName = promotion.promotionName,
        pid = promotion.promotionParameters.promotionId,
        promotionDescription = promotion.promotionDescription,
        tokenHash = TokenDsidHashMaker.hashToken(token, pp),
        streakCount = token.toBigIntVector().get(0).toInt(),
        lastDate = StreakDate.fromLong(token.toBigIntVector().get(1).toLong()),
        tokenUpdates = tokenUpdates,
        streakInterval = promotion.interval,
        tokenJson = tokenToJsonString(token)
    )


    val lastEpochDay = lastDate.toLong()
    val todayEpochDay = StreakTokenUpdateTimestamp.now()!!.timestamp!!
    override val points: Vector<BigInteger> =
        Vector.of(
            BigInteger.valueOf(streakCount.toLong()),
            BigInteger.valueOf(lastEpochDay)
        )
    val streakStillValid = (todayEpochDay - lastEpochDay <= 7)
    val deadline: StreakDate =
        StreakDate.fromLong(if (lastEpochDay == 0L) 0 else lastEpochDay.plus(streakInterval))
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

        fun StreakDate.toLong(): Long = when (this) {
            is NONE -> 0
            is DATE -> ChronoUnit.DAYS.between(epochZero, this.date)
        }
    }

    override fun toString(): String =
        when (this) {
            is NONE -> "None"
            is DATE -> date.toString()
        }
}


data class VipPromotionData(
    override val promotionName: String,
    override val pid: BigInteger,
    override val promotionDescription: String,
    override val tokenUpdates: List<TokenUpdate>,
    override val tokenHash: String,
    override val tokenJson: String,
    val score: Int,
    val vipLevel: VipStatus,
    val bronzeScore: Int,
    val silverScore: Int,
    val goldScore: Int
) : PromotionData {

    constructor(
        promotion: VipPromotion,
        token: Token,
        tokenUpdates: List<TokenUpdate>,
        pp: IncentivePublicParameters
    ) : this(
        promotionName = promotion.promotionName,
        pid = promotion.promotionParameters.promotionId,
        promotionDescription = promotion.promotionDescription,
        tokenUpdates = tokenUpdates,
        tokenHash = TokenDsidHashMaker.hashToken(token, pp),
        score = token.toBigIntVector().get(0).toInt(),
        vipLevel = VipStatus.fromInt(
            token.toBigIntVector().get(1).toInt()
        ),
        tokenJson = tokenToJsonString(token),
        bronzeScore = computeScoreForLevel(
            tokenUpdates,
            VipStatus.BRONZE
        ),
        silverScore = computeScoreForLevel(
            tokenUpdates,
            VipStatus.SILVER
        ),
        goldScore = computeScoreForLevel(
            tokenUpdates,
            VipStatus.GOLD
        )
    )


    override val points: Vector<BigInteger> =
        Vector.of(score.toBigInteger(), vipLevel.statusValue.toBigInteger())
    override val promotionImageUrl: String
        get() = "https://cdn.pixabay.com/photo/2017/10/11/07/18/eat-2840156_960_720.jpg"
}


interface TokenUpdate {
    val description: String
    val feasibility: PromotionUpdateFeasibility

    fun isFeasible(): Boolean = feasibility != PromotionUpdateFeasibility.NOT_APPLICABLE
    fun isSelected(): Boolean = feasibility == PromotionUpdateFeasibility.SELECTED
}

data class NoTokenUpdate(
    override val feasibility: PromotionUpdateFeasibility = PromotionUpdateFeasibility.CANDIDATE,
) : TokenUpdate {
    override val description: String = "Nothing"
}

data class EarnTokenUpdate(
    override val feasibility: PromotionUpdateFeasibility,
    override val description: String,
    val currentPoints: Vector<Int>,
    val addedPoints: Vector<Int>,
    val targetPoints: Vector<Int>
) : TokenUpdate

interface ZkpTokenUpdate : TokenUpdate {
    val zkpUpdateId: UUID
    val sideEffect: Optional<String>
}

data class HazelTokenUpdateState(
    override val zkpUpdateId: UUID,
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val current: Int,
    val goal: Int,
    val basketPoints: Int
) : ZkpTokenUpdate

data class ProveVipTokenUpdateState(
    override val zkpUpdateId: UUID,
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val currentStatus: VipStatus,
    val requiredStatus: VipStatus
) : ZkpTokenUpdate

data class UpgradeVipTokenUpdateState(
    override val zkpUpdateId: UUID,
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val currentPoints: Int,
    val requiredPoints: Int,
    val targetVipStatus: VipStatus,
    val currentVipStatus: VipStatus,
) : ZkpTokenUpdate

data class StandardStreakTokenUpdateState(
    override val zkpUpdateId: UUID,
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val lastDate: StreakDate,
    val newLastDate: LocalDate,
    val currentStreak: Int,
    val newCurrentStreak: Int,
    val intervalDays: Int
) : ZkpTokenUpdate {
    companion object {
        operator fun invoke(
            zkpUpdateId: UUID,
            description: String,
            sideEffect: Optional<String>,
            feasibility: PromotionUpdateFeasibility,
            tokenPoints: Vector<BigInteger>,
            intervalDays: Int
        ): ZkpTokenUpdate {
            val currentStreak = tokenPoints.get(0).toInt()
            val lastDate = StreakDate.fromLong(tokenPoints.get(1).toLong())
            val today = LocalDate.now()
            val newStreak =
                if (lastDate is StreakDate.DATE && Duration.between(today, lastDate.date)
                        .toDays() <= intervalDays
                ) currentStreak + 1 else 1
            return StandardStreakTokenUpdateState(
                zkpUpdateId,
                description,
                sideEffect,
                feasibility,
                lastDate,
                today,
                currentStreak,
                newStreak,
                intervalDays
            )
        }

    }
}

data class RangeProofStreakTokenUpdateState(
    override val zkpUpdateId: UUID,
    override val description: String,
    override val sideEffect: Optional<String>,
    override val feasibility: PromotionUpdateFeasibility,
    val lastDate: StreakDate,
    val newLastDate: LocalDate,
    val requiredStreak: Int,
    val currentStreak: Int,
    val newCurrentStreak: Int,
    val intervalDays: Int
) : ZkpTokenUpdate {
    companion object {
        operator fun invoke(
            zkpUpdateId: UUID,
            description: String,
            sideEffect: Optional<String>,
            feasibility: PromotionUpdateFeasibility,
            tokenPoints: Vector<BigInteger>,
            requiredStreak: Int,
            intervalDays: Int
        ): ZkpTokenUpdate {
            val currentStreak = tokenPoints.get(0).toInt()
            val lastStreakDate = StreakDate.fromLong(tokenPoints.get(1).toLong())
            val today = LocalDate.now()
            return RangeProofStreakTokenUpdateState(
                zkpUpdateId,
                description,
                sideEffect,
                feasibility,
                lastStreakDate,
                today,
                requiredStreak,
                currentStreak,
                currentStreak + 1,
                intervalDays
            )
        }
    }
}

data class SpendStreakTokenUpdateState(
    override val zkpUpdateId: UUID,
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
        fun fromInt(value: Int) =
            try {
                values().first { it.statusValue == value }
            } catch (e: Exception) {
                Timber.e(e)
                NONE
            }
    }
}

private fun computeScoreForLevel(tokenUpdates: List<TokenUpdate>, level: VipStatus): Int {
    val updateToLevel =
        tokenUpdates.find { tokenUpdate -> tokenUpdate is UpgradeVipTokenUpdateState && tokenUpdate.targetVipStatus == level }!! as UpgradeVipTokenUpdateState
    return updateToLevel.requiredPoints
}
