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
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.PromotionUserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.SerializableUserChoice
import org.cryptimeleon.incentive.app.domain.model.UserPromotionState
import org.cryptimeleon.incentive.app.domain.usecase.AnalyzeUserTokenUpdatesUseCase
import org.cryptimeleon.incentive.app.domain.usecase.GetPromotionStatesUseCase
import org.cryptimeleon.incentive.app.util.SLE
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    private val basketRepository: BasketRepository,
    private val promotionRepository: PromotionRepository,
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

    val userPromotionStates: Flow<List<UserPromotionState>> = GetPromotionStatesUseCase(
        promotionRepository,
        cryptoRepository,
        basketRepository
    )()

    val tokenUpdateChoices: Flow<List<PromotionUserUpdateChoice>> =
        AnalyzeUserTokenUpdatesUseCase(promotionRepository, cryptoRepository, basketRepository)()

    fun setUpdateChoice(
        promotionId: BigInteger,
        userUpdateChoice: SerializableUserChoice.UserUpdateChoice
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                promotionRepository.putUserUpdateChoice(promotionId, userUpdateChoice)
            }
        }
    }

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

    fun onDiscardClicked() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                basketRepository.discardCurrentBasket(true)
            }
        }
    }
}
