package org.cryptimeleon.incentive.app.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    promotionRepository: IPromotionRepository,
    private val basketRepository: IBasketRepository,
    private val preferencesRepository: IPreferencesRepository,
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

    private val resetAppUseCase =
        ResetAppUseCase(cryptoRepository, basketRepository, promotionRepository)

    private val _returnCode: MutableStateFlow<PayAndRedeemStatus?> = MutableStateFlow(null)
    val returnCode: StateFlow<PayAndRedeemStatus?>
        get() = _returnCode

    val promotionData: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()
    val basket = basketRepository.basket

    fun startPayAndRedeem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _checkoutStep.value = CheckoutStep.PROCESSING
                _returnCode.value = payAndRedeemUseCase.invoke()
                _checkoutStep.value = CheckoutStep.FINISHED
            }
        }
    }

    fun deleteBasket() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                basketRepository.discardCurrentBasket()
            }
        }
    }

    fun disableDSAndRecover() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Disable double spending attack
                preferencesRepository.updateDiscardUpdatedToken(false)
                // Recover, i.e. make sure the current token will not be blocked by DSP
                resetAppUseCase()
            }
        }
    }
}

enum class CheckoutStep {
    SUMMARY,
    PROCESSING,
    FINISHED,
}
