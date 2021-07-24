package org.cryptimeleon.incentive.app.scan

import android.app.Application
import android.icu.text.NumberFormat
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.network.BasketApi
import org.cryptimeleon.incentive.app.network.BasketItem
import org.cryptimeleon.incentive.app.network.Item
import org.cryptimeleon.incentive.app.database.basket.BasketDatabase
import timber.log.Timber
import java.util.*

/**
 * View model for the dialog holding the scanned item.
 * Allows changing the count (amount) and adding it to the basket.
 */
class ScanResultViewModel(private val item: Item, application: Application) :
    AndroidViewModel(application) {

    // Currency formatting
    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

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
        Timber.i("Add $title $_amount times to basket!")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val basketId =
                    BasketDatabase.getInstance(getApplication()).basketDatabaseDao().getBasket()
                val basketItem = BasketItem(basketId.basketId, _amount.value!!, barcode)
                val putItemResponse = BasketApi.retrofitService.putItemToBasket(basketItem)
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