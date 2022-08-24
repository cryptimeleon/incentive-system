package org.cryptimeleon.incentive.app.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemUseCase
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    basketRepository: IBasketRepository,
    promotionRepository: IPromotionRepository,
    preferencesRepository: IPreferencesRepository,
    application: Application
) : AndroidViewModel(application) {
    private val payAndRedeemUseCase =
        PayAndRedeemUseCase(
            promotionRepository,
            cryptoRepository,
            basketRepository,
            preferencesRepository
        )

    private val _checkoutStep = MutableStateFlow(CheckoutStep.SUMMARY)
    val checkoutStep: StateFlow<CheckoutStep>
        get() = _checkoutStep


    // store basketId since a new one is retrieved after payment
    private val _paidBasketId: MutableStateFlow<UUID?> = MutableStateFlow(null)
    val paidBasketId: StateFlow<UUID?>
        get() = _paidBasketId

    val promotionData: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()
    val basket = basketRepository.basket

    fun startPayAndRedeem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Store basket ID since use case will retrieve a new one
                _checkoutStep.value = CheckoutStep.PROCESSING
                _paidBasketId.value = basket.first()?.basketId
                payAndRedeemUseCase.invoke()
                _checkoutStep.value = CheckoutStep.FINISHED
            }
        }
    }
}

enum class CheckoutStep {
    SUMMARY,
    PROCESSING,
    FINISHED
}
