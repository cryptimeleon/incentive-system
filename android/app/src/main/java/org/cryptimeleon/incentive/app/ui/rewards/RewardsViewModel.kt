package org.cryptimeleon.incentive.app.ui.rewards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.UpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UserPromotionState
import org.cryptimeleon.incentive.app.domain.model.UserUpdateChoice
import org.cryptimeleon.incentive.app.domain.usecase.AnalyzeUserTokenUpdatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.GetPromotionStatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import org.cryptimeleon.incentive.promotion.sideeffect.NoSideEffect
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    basketRepository: BasketRepository,
    private val promotionRepository: PromotionRepository,
    application: Application
) : AndroidViewModel(application) {

    val promotionDataListFlow: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()
    val state: Flow<RewardsState> = combine(
        basketRepository.basket,
        GetPromotionStatesUseCase(
            promotionRepository,
            cryptoRepository,
            basketRepository
        ).invoke(),
        AnalyzeUserTokenUpdatesUseCase(
            promotionRepository,
            cryptoRepository,
            basketRepository
        ).invoke(),
        basketRepository.rewardItems
    ) { basket: Basket?, userPromotionStates: List<UserPromotionState>, promotionUserUpdates: List<PromotionUserUpdateChoice>, rewardItems: List<RewardItem> ->
        when {
            basket == null -> {
                emptyList()
            }
            rewardItems.isEmpty() -> {
                basketRepository.refreshRewardItems()
                emptyList()
            }
            else -> {
                userPromotionStates.filter { it.qualifiedUpdates.size > 1 }.map {
                    PromotionInfo(
                        it.promotionId,
                        it.promotionName,
                        it.qualifiedUpdates.map { updateChoice ->
                            val userUpdateChoice = updateChoice.toUserUpdateChoice()
                            val isSelected =
                                promotionUserUpdates.any { x -> x.promotionId == it.promotionId && x.userUpdateChoice == updateChoice.toUserUpdateChoice() }
                            when (updateChoice) {
                                is UpdateChoice.None -> Choice(
                                    "Nothing",
                                    "No cryptographic protocols are executed. The token remains unchanged.",
                                    NoChoiceSideEffect,
                                    userUpdateChoice,
                                    isSelected
                                )
                                is UpdateChoice.Earn -> Choice(
                                    "Collect ${updateChoice.pointsToEarn} points",
                                    "Use the fast-earn protocol to add ${updateChoice.pointsToEarn} to the points vector of the token and update the SPSEQ signature accordingly",
                                    NoChoiceSideEffect,
                                    userUpdateChoice,
                                    isSelected
                                )
                                is UpdateChoice.ZKP -> Choice(
                                    updateChoice.updateDescription,
                                    "Run the ZKP with id ${updateChoice.updateId} to get change the points vector from ${updateChoice.oldPoints} to ${updateChoice.newPoints}.",
                                    when (val sideEffect = updateChoice.sideEffect) {
                                        is NoSideEffect -> NoChoiceSideEffect
                                        is RewardSideEffect -> {
                                            val rewardItem =
                                                rewardItems.find { rewardItem -> rewardItem.id == sideEffect.rewardId }!!
                                            RewardChoiceSideEffect(rewardItem.id, rewardItem.title)
                                        }
                                        else -> throw RuntimeException("Unexpected subtype of SideEffect")
                                    },
                                    userUpdateChoice,
                                    isSelected
                                )
                            }
                        }
                    )
                }
            }
        }
    }.map {
        RewardsState(it)
    }

    fun setUpdateChoice(promotionId: BigInteger, userUpdateChoice: UserUpdateChoice) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                promotionRepository.putUserUpdateChoice(promotionId, userUpdateChoice)
            }
        }
    }

}

data class RewardsState(val promotionInfos: List<PromotionInfo>)
data class PromotionInfo(
    val promotionId: BigInteger,
    val promotionName: String,
    val choices: List<Choice>
)

// There must always be some default choice that does not 'harm' the user's token
// This is either None, Earn, or a ZKP with only positive side effects
data class Choice(
    val humanReadableDescription: String,
    val cryptographicDescription: String,
    val sideEffect: ChoiceSideEffect,
    val userUpdateChoice: UserUpdateChoice,
    val isSelected: Boolean
)

sealed interface ChoiceSideEffect
object NoChoiceSideEffect : ChoiceSideEffect
data class RewardChoiceSideEffect(val id: String, val title: String) : ChoiceSideEffect
