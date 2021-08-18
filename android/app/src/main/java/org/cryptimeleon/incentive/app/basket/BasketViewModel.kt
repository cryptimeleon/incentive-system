package org.cryptimeleon.incentive.app.basket

import android.app.Application
import android.icu.text.NumberFormat
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.basket.BasketItemRecyclerViewAdapter.BasketListItem
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.network.Basket
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val basketRepository: BasketRepository,
    application: Application
) : AndroidViewModel(application) {

    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    private val _basketContent = MutableLiveData<List<BasketListItem>>(null)
    val basketContent: LiveData<List<BasketListItem>>
        get() = _basketContent

    private val _basket = MutableLiveData<Basket?>()
    val basket: LiveData<Basket?>
        get() = _basket

    val basketValue = Transformations.map(_basket) {
        it?.let {
            currencyFormat.format(it.value / 100.0)
        }
    }

    val showBasketActionButtons = Transformations.map(_basketContent) {
        (it != null && it.isNotEmpty())
    }

    val visibleIfBasketEmpty = Transformations.map(_basketContent) {
        if (it != null && it.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    val visibleIfBasketNotEmpty = Transformations.map(_basketContent) {
        if (it != null && it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _basket.postValue(basketRepository.getActiveBasket())
                _basketContent.postValue(basketRepository.getCurrentBasketContents())
            }
        }
    }

    fun setItemCount(itemId: String, count: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (basketRepository.setBasketItemCount(itemId, count)) {
                    _basket.postValue(basketRepository.getActiveBasket())
                    _basketContent.postValue(basketRepository.getCurrentBasketContents())
                } else {
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
                _basket.postValue(basketRepository.getActiveBasket())
                _basketContent.postValue(basketRepository.getCurrentBasketContents())
            }
        }
    }

    fun payAndRedeem() {
        // TODO continue working on this functionality
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Store basket as it will be replaced by payment
                val basket = basket.value!!

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

                    _basket.postValue(basketRepository.getActiveBasket())
                    _basketContent.postValue(basketRepository.getCurrentBasketContents())
                }
            }
        }
    }
}
