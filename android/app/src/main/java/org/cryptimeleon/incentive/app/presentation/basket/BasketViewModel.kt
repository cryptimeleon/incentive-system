package org.cryptimeleon.incentive.app.presentation.basket

import android.app.Application
import android.icu.text.NumberFormat
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import org.cryptimeleon.incentive.app.util.SLE
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val basketRepository: BasketRepository,
    application: Application
) : AndroidViewModel(application) {

    val basket = basketRepository.basket.map {
        SLE.Success(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SLE.Loading()
    )


    fun setItemCount(itemId: UUID, count: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (!basketRepository.setBasketItemCount(itemId, count)) {
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
                basket.collect {
                    when (it) {
                        is SLE.Success -> {
                            val basket = it.data!! // cannot be null by constructor of Success

                            if (basketRepository.payCurrentBasket()) {
                                // Redeem basket
                                cryptoRepository.runCreditEarn(
                                    basket.basketId,
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
}

/**
 * Simple class for data binding
 */
class BasketListItem(basketItem: BasketItem, var count: Int) {
    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    val priceSingle: String = currencyFormat.format(basketItem.price / 100.0)
    val priceTotal: String = currencyFormat.format(basketItem.price * count / 100.0)
    val countStr: String = count.toString()
}
