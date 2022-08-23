package org.cryptimeleon.incentive.app.ui.basket

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.ZKP
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import org.cryptimeleon.incentive.app.util.SLE
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val basketRepository: IBasketRepository,
    private val promotionRepository: IPromotionRepository,
    cryptoRepository: ICryptoRepository,
    application: Application
) : AndroidViewModel(application) {

    val basket: Flow<SLE<Basket>> = basketRepository.basket.map {
        Timber.i("Basket ${it}, ${it?.items}")
        if (it == null) {
            SLE.Error("Basket is null!")
        } else {
            SLE.Success(it)
        }
    }

    val promotionData: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()

    fun setItemCount(itemId: String, count: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (!basketRepository.putItemIntoCurrentBasket(itemId, count)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Could not update item $itemId",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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
}
