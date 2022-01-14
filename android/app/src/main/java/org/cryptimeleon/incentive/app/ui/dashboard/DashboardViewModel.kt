package org.cryptimeleon.incentive.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.hazel.HazelReward
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    promotionRepository: PromotionRepository,
    application: Application
) :
    AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                promotionRepository.reloadPromotions()
            }
        }
    }

    val state: StateFlow<DashboardState> = promotionRepository.promotions
        .combine(cryptoRepository.tokens) { promotions: List<Promotion>, tokens: List<Token> ->
            DashboardState(
                promotions.mapNotNull { promotion ->
                    val token: Token =
                        tokens.find { promotion.promotionParameters.promotionId == it.promotionId }!!
                    if (promotion is HazelPromotion) {
                        val count = token.points.get(0).asInteger().toInt()
                        HazelPromotionState(
                            id = promotion.promotionParameters.promotionId.toString(),
                            title = promotion.promotionName,
                            description = promotion.promotionDescription,
                            rewards = promotion.rewards.mapNotNull {
                                if (it is HazelReward) {
                                    HazelRewardState(
                                        it.rewardDescription,
                                        it.rewardSideEffect.name,
                                        count,
                                        it.rewardCost
                                    )
                                } else {
                                    null
                                }
                            },
                            count = count,
                        )
                    } else {
                        Timber.i("Promotion not yet implemented")
                        null
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
    abstract val rewards: List<RewardState>
}

data class HazelPromotionState(
    override val id: String,
    override val title: String,
    override val description: String,
    override val rewards: List<RewardState>,
    val count: Int,
) : PromotionState()


sealed class RewardState {
    abstract val description: String
    abstract val sideEffect: String
}

data class HazelRewardState(
    override val description: String,
    override val sideEffect: String,
    val current: Int,
    val goal: Int
) : RewardState()
