package org.cryptimeleon.incentive.app.ui.basket

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.util.SLE
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val basketRepository: BasketRepository,
    application: Application
) : AndroidViewModel(application) {

    val basket: Flow<SLE<Basket>> = basketRepository.basket.map {
            Timber.i("Basket $it!!")
            if (it == null) {
                SLE.Error("Basket is null!")
            } else {
                SLE.Success(it)
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

    fun payAndRedeem() {
        // TODO continue working on this functionality
        // Store basket as it will be replaced by payment
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val currentBasket = basket.first()) {
                    is SLE.Success -> {
                        val basket =
                            currentBasket.data!! // cannot be null by constructor of Success

                        if (basketRepository.payCurrentBasket()) {
                            // Redeem basket
                            cryptoRepository.runCreditEarn(
                                basket.basketId,
                                // TODO this needs to be adapted!
                                PromotionParameters(BigInteger.ONE, 1),
                                basket.value
                            )

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    getApplication(), "Successfully paid and redeemed basket!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                getApplication(), "An error occurred",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
