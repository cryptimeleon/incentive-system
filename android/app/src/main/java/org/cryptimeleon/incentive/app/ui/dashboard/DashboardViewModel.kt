package org.cryptimeleon.incentive.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import org.cryptimeleon.incentive.crypto.model.Token
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
import java.math.BigInteger
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    promotionRepository: PromotionRepository,
    basketRepository: BasketRepository,
    application: Application
) :
    AndroidViewModel(application) {

    val promotionDataListFlow: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()

    fun onCardClicked(promotionId: BigInteger) {
        Timber.i("Card with id ${promotionId.toInt()} clicked")
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
