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
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.ZKP
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemStatus
import org.cryptimeleon.incentive.app.domain.usecase.PayAndRedeemUseCase
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import org.cryptimeleon.incentive.app.domain.usecase.ResetAppUseCase
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    basketRepository: IBasketRepository,
    promotionRepository: IPromotionRepository,
    preferencesRepository: IPreferencesRepository,
    private val basketRepository: IBasketRepository,
    private val promotionRepository: IPromotionRepository,
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
    private val resetAppUseCase =
        ResetAppUseCase(cryptoRepository, basketRepository, promotionRepository)

    private val _checkoutStep = MutableStateFlow(CheckoutStep.REWARDS)
    val checkoutStep: StateFlow<CheckoutStep>
        get() = _checkoutStep


    // store basketId since a new one is retrieved after payment
    private val _paidBasketId: MutableStateFlow<UUID?> = MutableStateFlow(null)
    val paidBasketId: StateFlow<UUID?>
        get() = _paidBasketId

    private val _returnCode: MutableStateFlow<PayAndRedeemStatus?> = MutableStateFlow(null)
    val returnCode: StateFlow<PayAndRedeemStatus?>
        get() = _returnCode

    val promotionData: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()
    val basket = basketRepository.basket

    private val _resetAppFinished = MutableStateFlow(false)

    fun gotoSummary() {
        _checkoutStep.value = CheckoutStep.SUMMARY
    }

    fun startPayAndRedeem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Store basket ID since use case will retrieve a new one
                _checkoutStep.value = CheckoutStep.PROCESSING
                _paidBasketId.value = basket.first()?.basketId
                _returnCode.value = payAndRedeemUseCase.invoke()
                _checkoutStep.value = CheckoutStep.FINISHED
            }
        }
    }

    fun setUpdateChoice(promotionId: BigInteger, tokenUpdate: TokenUpdate) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val userUpdateChoice = when (tokenUpdate) {
                    is NoTokenUpdate -> None
                    is EarnTokenUpdate -> Earn
                    is ZkpTokenUpdate -> ZKP(tokenUpdate.zkpUpdateId)
                    else -> {
                        throw RuntimeException("Unknown token update $tokenUpdate")
                    }
                }
                promotionRepository.putUserUpdateChoice(promotionId, userUpdateChoice)
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
