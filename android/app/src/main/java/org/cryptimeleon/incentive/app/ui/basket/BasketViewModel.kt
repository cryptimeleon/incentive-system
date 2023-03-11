package org.cryptimeleon.incentive.app.ui.basket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.ZKP
import org.cryptimeleon.incentive.app.domain.usecase.*
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val basketRepository: IBasketRepository,
    private val promotionRepository: IPromotionRepository,
    cryptoRepository: ICryptoRepository,
    application: Application
) : AndroidViewModel(application) {

    val basket: Flow<Basket> = basketRepository.basket

    val promotionData: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()

    fun setItemCount(itemId: String, count: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                basketRepository.putItemIntoBasket(itemId, count)
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

    fun discardCurrentBasket() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                basketRepository.discardCurrentBasket()
            }
        }
    }
}
