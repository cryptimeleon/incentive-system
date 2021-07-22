package org.cryptimeleon.incentive.app.scan

import android.icu.text.NumberFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.cryptimeleon.incentive.app.network.Item
import timber.log.Timber
import java.util.*

class ScanResultViewModel(private val item: Item) : ViewModel() {

    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    val barcode: String = item.id.toString()
    val title: String = item.title
    val priceSingle: String = currencyFormat.format(item.price / 100.0)

    private val _closeScanResult = MutableLiveData<Boolean>(false)
    val closeScanResult: LiveData<Boolean>
        get() = _closeScanResult

    // The number of items to add
    private val _amount = MutableLiveData(1)
    val priceTotal = Transformations.map(_amount) {
        currencyFormat.format(it * item.price / 100.0)
    }

    fun onAmountChange(newAmount: Int) {
        if (newAmount != _amount.value)
            _amount.value = newAmount
    }

    fun onAddToBasket() {
        Timber.i("Add $title $_amount times to basket!")
        _closeScanResult.value = true
    }

    fun closeScanResultFinished() {
        _closeScanResult.value = false
    }
}