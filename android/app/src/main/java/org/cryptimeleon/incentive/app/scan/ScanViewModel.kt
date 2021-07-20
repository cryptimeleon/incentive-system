package org.cryptimeleon.incentive.app.scan

import android.icu.text.NumberFormat
import android.util.Base64.DEFAULT
import android.util.Base64.decode
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.network.BasketApi
import org.cryptimeleon.incentive.app.network.Item
import timber.log.Timber
import java.math.BigInteger
import java.util.*

/**
 * ViewModel for the ScanFragment.
 * Handles detected barcodes, queries the product, processes the product and allows adding the basket.
 * In the future, it could find related promotions based on the current basket content, tokens and
 * scanned item, e.g. add three of these to get one for free etc.
 */
class ScanViewModel : ViewModel() {
    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    private val _barcode = MutableLiveData<UUID>()
    private val _item = MutableLiveData<Item>()
    val barcode = Transformations.map(_barcode) {
        it.toString()
    }

    val title = Transformations.map(_item) {
        _item.value?.title
    }

    // price in cents for avoiding rounding issues
    private val _priceSingle = Transformations.map(_item) {
        _item.value?.price
    }

    private val _amount = MutableLiveData(1)
    val priceSingle =
        Transformations.map(_priceSingle) { it?.let { currencyFormat.format(it / 100.0) } }
    val priceTotal: LiveData<String> = MediatorLiveData<String>()
        .apply {
            fun update() {
                val amount = _amount.value ?: return
                val price = _priceSingle.value ?: return

                this.value = currencyFormat.format(amount * price / 100.0)
            }
            addSource(_amount) { update() }
            addSource(_priceSingle) { update() }
            update()
        }

    fun setBarcode(barcodeStr: String) {
        Timber.i(barcodeStr)
        val barcode: UUID
        try {
            // Barcode is base64 encoded UUID, parse back to UUID
            val bytes = decode(barcodeStr, DEFAULT)
            val hex = String.format("%040x", BigInteger(1, bytes)).takeLast(32)
            val uuid = StringBuilder(hex)
                .insert(20, '-')
                .insert(16, '-')
                .insert(12, '-')
                .insert(8, '-')
                .toString()
            barcode = UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            Timber.i(e)
            return
        }

        if (barcode == _barcode.value) return

        _barcode.value = barcode
        // query basket item from basket service and check if it exists
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val allItems = BasketApi.retrofitService.getAllItems()
                allItems.body()?.forEach {
                    Timber.i(it.toString())
                    if (it.id == barcode) {
                        _item.postValue(it)
                    }
                } // todo replace by new basket service api call
            }
            // check if promotions apply or can be applied by adding some more of this item
        }
    }

    fun onAmountChange(newAmount: Int) {
        if (newAmount != _amount.value)
            _amount.value = newAmount
    }
}