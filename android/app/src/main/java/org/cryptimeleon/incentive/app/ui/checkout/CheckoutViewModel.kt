package org.cryptimeleon.incentive.app.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
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
    var payAndRedeemState = MutableStateFlow(PayAndRedeemState.NOT_STARTED)

    fun startPayAndRedeem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                payAndRedeemUseCase.invoke().collect { payAndRedeemState.emit(it) }
            }
        }
    }

}
