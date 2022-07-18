package org.cryptimeleon.incentive.app.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.RewardItem
import org.cryptimeleon.incentive.app.domain.model.SerializableUserChoice
import org.cryptimeleon.incentive.app.domain.model.UpdateChoice
import org.cryptimeleon.incentive.app.domain.model.UserPromotionState
import org.cryptimeleon.incentive.app.domain.usecase.AnalyzeUserTokenUpdatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.GetPromotionStatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemUseCase
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    basketRepository: BasketRepository,
    promotionRepository: PromotionRepository,
    application: Application
) : AndroidViewModel(application) {
    private val payAndRedeemUseCase =
        PayAndRedeemUseCase(promotionRepository, cryptoRepository, basketRepository)
    private val promotionStatesFlow =
        GetPromotionStatesUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()
    private val tokenUpdateChoicesFlow: Flow<List<PromotionUserUpdateChoice>> =
        AnalyzeUserTokenUpdatesUseCase(promotionRepository, cryptoRepository, basketRepository)()
    private val basket = basketRepository.basket
    private val rewardItems = basketRepository.rewardItems

    // store basketId since a new one is retrieved after payment
    private val _paidBasketId: MutableStateFlow<UUID?> = MutableStateFlow(null)
    val paidBasketId: StateFlow<UUID?>
        get() = _paidBasketId

    val payAndRedeemState = MutableStateFlow(PayAndRedeemState.NOT_STARTED)
    val checkoutState: Flow<CheckoutState> = combine(
        tokenUpdateChoicesFlow,
        promotionStatesFlow,
        rewardItems
    ) { tokenUpdateChoices, promotionStates, rewards ->
        combineTokenUpdatesWithPromotions(promotionStates, tokenUpdateChoices, rewards)
    }.combine(basket) { checkoutPromotionStates, basket ->
        combinePromotionStatesWIthBasket(
            basket,
            checkoutPromotionStates
        )
    }
    val promotionData: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()

    private fun combineTokenUpdatesWithPromotions(
        promotionStates: List<UserPromotionState>,
        tokenUpdateChoices: List<PromotionUserUpdateChoice>,
        rewardItems: List<RewardItem>
    ) = promotionStates.mapNotNull {
        val choice =
            tokenUpdateChoices.find { choice -> choice.promotionId == it.promotionId }
                ?: return@mapNotNull null
        val rewardDescription = when (choice.userUpdateChoice) {
            is SerializableUserChoice.ZKP -> {
                val tokenUpdateId = choice.userUpdateChoice.tokenUpdateId
                val updateChoiceOrNull =
                    it.qualifiedUpdates.find { updateChoice -> updateChoice is UpdateChoice.ZKP && updateChoice.updateId == tokenUpdateId }
                        ?: return@mapNotNull null
                val updateChoice: UpdateChoice.ZKP = updateChoiceOrNull as UpdateChoice.ZKP
                when (updateChoice.sideEffect) {
                    is RewardSideEffect -> rewardItems.find { rewardItem: RewardItem -> rewardItem.id == updateChoice.sideEffect.rewardId }?.title
                    else -> null
                }
            }
            else -> null
        }
        return@mapNotNull CheckoutPromotionState(
            it.promotionName,
            choice.toString(),
            rewardDescription
        )
    }

    private fun combinePromotionStatesWIthBasket(
        basket: Basket?,
        checkoutPromotionStates: List<CheckoutPromotionState>
    ) = if (basket == null) {
        CheckoutState(
            promotionStates = checkoutPromotionStates,
            basketState = BasketState(
                0,
                "",
                emptyList()
            )
        )
    } else {
        CheckoutState(
            promotionStates = checkoutPromotionStates,
            basketState = BasketState(
                basket.value,
                basket.basketId.toString(),
                basket.items.map { item ->
                    BasketItem(
                        item.title,
                        item.count,
                        item.price,
                        item.price * item.count
                    )
                }
            )
        )
    }

    fun startPayAndRedeem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Store basket ID since use case will retrieve a new one
                _paidBasketId.value = basket.first()?.basketId
                payAndRedeemUseCase.invoke().collect { payAndRedeemState.emit(it) }
            }
        }
    }
}

data class CheckoutState(
    val promotionStates: List<CheckoutPromotionState>,
    val basketState: BasketState
)

data class BasketState(
    val basketValue: Int,
    val basketId: String,
    val basketItems: List<BasketItem>
)

data class BasketItem(
    // TODO replace by default class
    val title: String,
    val count: Int,
    val costSingle: Int,
    val costTotal: Int,
)

// Description to display what happens for every state
data class CheckoutPromotionState(
    val promotionName: String,
    val choiceDescription: String,
    val rewardDescription: String? = null
)
