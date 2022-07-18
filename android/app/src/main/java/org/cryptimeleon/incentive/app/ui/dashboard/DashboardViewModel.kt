package org.cryptimeleon.incentive.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.RangeProofStreakTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.SpendStreakTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.StandardStreakTokenUpdate
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion
import org.cryptimeleon.incentive.promotion.vip.ProveVipTokenUpdate
import org.cryptimeleon.incentive.promotion.vip.UpgradeVipZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.vip.VipPromotion
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    promotionRepository: PromotionRepository,
    application: Application
) :
    AndroidViewModel(application) {

    val state: StateFlow<DashboardState> = promotionRepository.promotions
        .combine(cryptoRepository.tokens) { promotions: List<Promotion>, tokens: List<Token> ->
            DashboardState(
                promotions.mapNotNull { promotion ->
                    val token: Token =
                        tokens.find { promotion.promotionParameters.promotionId == it.promotionId }!!
                    when (promotion) {
                        is HazelPromotion -> {
                            HazelPromotionState.fromPromotion(promotion, token)
                        }
                        is VipPromotion -> {
                            VipPromotionState.fromPromotion(promotion, token)
                        }
                        is StreakPromotion -> {
                            StreakPromotionState.fromPromotion(promotion, token)
                        }
                        else -> {
                            Timber.i("Promotion not yet implemented")
                            null
                        }
                    }
                }
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState(emptyList())
        )

}

data class DashboardState(val promotionStates: List<PromotionState>)

sealed class PromotionState {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    abstract val updates: List<TokenUpdateState>
}

data class HazelPromotionState(
    override val id: String,
    override val title: String,
    override val description: String,
    override val updates: List<TokenUpdateState>,
    val count: Int,
) : PromotionState() {
    companion object {
        fun fromPromotion(promotion: HazelPromotion, token: Token): HazelPromotionState {
            val count = token.points.get(0).asInteger().toInt()
            return HazelPromotionState(
                id = promotion.promotionParameters.promotionId.toString(),
                title = promotion.promotionName,
                description = promotion.promotionDescription,
                updates = promotion.zkpTokenUpdates.mapNotNull {
                    if (it is HazelTokenUpdate) {
                        HazelTokenUpdateState(
                            it.rewardDescription,
                            it.sideEffect.toString(),
                            count,
                            it.rewardCost
                        )
                    } else {
                        null
                    }
                },
                count = count,
            )
        }
    }
}

data class VipPromotionState(
    override val id: String,
    override val title: String,
    override val description: String,
    override val updates: List<TokenUpdateState>,
    val points: Int,
    val status: VipStatus
) : PromotionState() {
    companion object {
        fun fromPromotion(promotion: VipPromotion, token: Token): VipPromotionState {
            val vipStatus = VipStatus.fromInt(token.points.get(1).asInteger().toInt())
            val points = token.points.get(1).asInteger().toInt()
            return VipPromotionState(
                id = promotion.promotionParameters.promotionId.toString(),
                title = promotion.promotionName,
                description = promotion.promotionDescription,
                updates = promotion.zkpTokenUpdates.mapNotNull {
                    when (it) {
                        is UpgradeVipZkpTokenUpdate -> {
                            UpgradeVipTokenUpdateState(
                                it.rewardDescription,
                                it.sideEffect.toString(),
                                points,
                                it.accumulatedCost,
                                VipStatus.fromInt(it.toVipStatus)
                            )
                        }
                        is ProveVipTokenUpdate -> {
                            VipTokenUpdateState(
                                it.rewardDescription,
                                it.sideEffect.toString(),
                                vipStatus,
                                VipStatus.fromInt(it.requiredStatus)
                            )
                        }
                        else -> {
                            null
                        }
                    }
                },
                points,
                vipStatus
            )
        }
    }
}

data class StreakPromotionState(
    override val id: String,
    override val title: String,
    override val description: String,
    override val updates: List<TokenUpdateState>,
    val streak: Int,
    val lastTimestamp: LocalDate
) : PromotionState() {
    companion object {
        fun fromPromotion(promotion: StreakPromotion, token: Token): StreakPromotionState {
            val streak = token.points.get(0).asInteger().toInt()
            val lastTimestamp = LocalDate.ofEpochDay(token.points.get(1).asInteger().toLong())
            return StreakPromotionState(
                id = promotion.promotionParameters.promotionId.toString(),
                title = promotion.promotionName,
                description = promotion.promotionDescription,
                updates = promotion.zkpTokenUpdates.mapNotNull {
                    when (it) {
                        is StandardStreakTokenUpdate -> {
                            StandardStreakTokenUpdateState(
                                it.rewardDescription,
                                it.sideEffect.toString(),
                            )
                        }
                        is RangeProofStreakTokenUpdate -> {
                            RangeProofStreakTokenUpdateState(
                                it.rewardDescription,
                                it.sideEffect.toString(),
                                it.lowerLimit,
                                streak
                            )
                        }
                        is SpendStreakTokenUpdate -> {
                            SpendStreakTokenUpdateState(
                                it.rewardDescription,
                                it.sideEffect.toString(),
                                it.cost,
                                streak
                            )
                        }
                        else -> {
                            null
                        }
                    }
                },
                streak,
                lastTimestamp
            )
        }
    }
}

sealed class TokenUpdateState {
    abstract val description: String
    abstract val sideEffect: String
}

data class HazelTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    val current: Int,
    val goal: Int
) : TokenUpdateState()

data class VipTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    val currentStatus: VipStatus,
    val requiredStatus: VipStatus
) : TokenUpdateState()

data class UpgradeVipTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    val currentPoints: Int,
    val requiredPoints: Int,
    val vipStatus: VipStatus,
) : TokenUpdateState()

data class StandardStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
) : TokenUpdateState()

data class RangeProofStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    val requiredStreak: Int,
    val currentStreak: Int
) : TokenUpdateState()

data class SpendStreakTokenUpdateState(
    override val description: String,
    override val sideEffect: String,
    val requiredStreak: Int,
    val currentStreak: Int
) : TokenUpdateState()

enum class VipStatus(val statusValue: Int) {
    NONE(0), // do not delete this!
    BRONZE(1),
    SILVER(2),
    GOLD(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.statusValue == value }
    }
}
