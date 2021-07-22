package org.cryptimeleon.incentive.app.scan

import android.icu.text.NumberFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.cryptimeleon.incentive.app.network.Item
import timber.log.Timber
import java.util.*

/**
 * View model for the dialog holding the scanned item.
 * Allows changing the count (amount) and adding it to the basket.
 */
class ScanResultViewModel(private val item: Item) : ViewModel() {

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
        // TODO implement some handling here
        _closeScanResult.value = true
    }

    /**
     * Close the scan result window.
     * To be called from the Fragment.
     */
    fun closeScanResultFinished() {
        _closeScanResult.value = false
    }
}