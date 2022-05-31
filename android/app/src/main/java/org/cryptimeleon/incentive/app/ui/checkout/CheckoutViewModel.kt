package org.cryptimeleon.incentive.app.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.usecase.AnalyzeUserTokenUpdatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.GetPromotionStatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemState
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemUseCase
import org.cryptimeleon.incentive.app.util.formatCents
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

    val payAndRedeemState = MutableStateFlow(PayAndRedeemState.NOT_STARTED)
    val checkoutState: Flow<CheckoutState> =
        tokenUpdateChoicesFlow.combine(
            promotionStatesFlow
        ) { tokenUpdateChoices, promotionStates ->
            promotionStates.map {
                val choice =
                    tokenUpdateChoices.find { choice -> choice.promotionId == it.promotionId }
                return@map CheckoutPromotionState(
                    it.promotionName,
                    choice.toString()
                )
            }
        }.combine(basket) { checkoutPromotionStates, basket ->
            if (basket == null) {
                CheckoutState(
                    promotionStates = checkoutPromotionStates,
                    basketState = BasketState(
                        "",
                        "",
                        emptyList()
                    )
                )
            } else {
                CheckoutState(
                    promotionStates = checkoutPromotionStates,
                    basketState = BasketState(
                        "${basket.value}",
                        basket.basketId.toString(),
                        basket.items.map { item ->
                            BasketItem(
                                item.title,
                                item.count,
                                formatCents(item.price),
                                formatCents(item.price * item.count)
                            )
                        }
                    )
                )
            }
        }

    fun startPayAndRedeem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
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
    val basketValue: String,
    val basketId: String,
    val basketItems: List<BasketItem>
)

data class BasketItem(
    val title: String,
    val count: Int,
    val costSingle: String,
    val costTotal: String,
)

// Description to display what happens for every state
data class CheckoutPromotionState(
    val promotionName: String,
    val choiceDescription: String
)
