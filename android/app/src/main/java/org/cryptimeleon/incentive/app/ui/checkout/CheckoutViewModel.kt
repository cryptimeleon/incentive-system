package org.cryptimeleon.incentive.app.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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

    private val payAndRedeemState = MutableStateFlow(PayAndRedeemState.NOT_STARTED)
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
        }.combine(payAndRedeemState) { checkoutPromotionStates, payAndRedeemState ->
            CheckoutState(
                payAndRedeemState = payAndRedeemState,
                promotionStates = checkoutPromotionStates
            )
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
    val payAndRedeemState: PayAndRedeemState,
    val promotionStates: List<CheckoutPromotionState>,
)

// Description to display what happens for every state
data class CheckoutPromotionState(
    val promotionName: String,
    val choiceDescription: String
)
