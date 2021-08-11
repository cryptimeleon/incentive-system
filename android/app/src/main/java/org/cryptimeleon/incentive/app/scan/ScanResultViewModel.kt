package org.cryptimeleon.incentive.app.scan

import android.app.Application
import android.icu.text.NumberFormat
import android.widget.Toast
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.network.BasketApiService
import org.cryptimeleon.incentive.app.network.BasketItem
import org.cryptimeleon.incentive.app.network.Item
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * View model for the dialog holding the scanned item.
 * Allows changing the count (amount) and adding it to the basket.
 */
@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val basketApiService: BasketApiService,
    application: Application,
    state: SavedStateHandle
) :
    AndroidViewModel(application) {

    // Currency formatting
    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    // TODO improve upon this
    val item: Item = state.get<Item>("item")!!
    val barcode: String = item.id
    val title: String = item.title
    val priceSingle: String = currencyFormat.format(item.price / 100.0)

    // Control closing the result window
    private val _closeScanResult = MutableLiveData(false)
    val closeScanResult: LiveData<Boolean>
        get() = _closeScanResult

    // The number of items to add
    private val _amount = MutableLiveData(1)

    // The total price, formatted as a currency
    val priceTotal = Transformations.map(_amount) {
        currencyFormat.format(it * item.price / 100.0)
    }

    /**
     * React on amount changes.
     *
     * @param newAmount the new amount
     */
    fun onAmountChange(newAmount: Int) {
        if (newAmount != _amount.value)
            _amount.value = newAmount
    }

    /**
     * Handle the add to basket action.
     * Add amount many of the item to the basket.
     */
    fun onAddToBasket() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val basket =
                    BasketDatabase.getInstance(getApplication()).basketDatabaseDao().getBasket()

                Timber.i("Add $title ${_amount.value} times to basket ${basket.basketId}!")

                val basketItem = BasketItem(basket.basketId, _amount.value!!, barcode)
                val putItemResponse = basketApiService.putItemToBasket(basketItem)
                withContext(Dispatchers.Main) {
                    if (putItemResponse.isSuccessful) {
                        Toast.makeText(
                            getApplication(),
                            "Successfully put ${item.id} to basket.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "An error occured when trying to put ${item.id} to basket.\nresponse code: ${putItemResponse.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                _closeScanResult.postValue(true)
            }
        }

    }

    /**
     * Close the scan result window.
     * To be called from the Fragment.
     */
    fun closeScanResultFinished() {
        _closeScanResult.value = false
    }
}