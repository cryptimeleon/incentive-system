package org.cryptimeleon.incentive.app.scan

import android.util.Base64.DEFAULT
import android.util.Base64.decode
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _barcode = MutableLiveData<UUID>()
    val item = MutableLiveData<Item>()
    val showItem = MutableLiveData(false)

    fun setBarcode(barcodeStr: String) {
        if (showItem.value == true) {
            return
        }

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

        if (_barcode.value == barcode) {
            return
        }
        _barcode.value = barcode
        // query basket item from basket service and check if it exists
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val allItems = BasketApi.retrofitService.getAllItems()
                allItems.body()?.forEach {
                    Timber.i(it.toString())
                    if (it.id == barcode) {
                        item.postValue(it)
                        showItem.postValue(true)
                    }
                } // todo replace by new basket service api call
            }
            // check if promotions apply or can be applied by adding some more of this item
        }
    }

    fun showItemFinished() {
        showItem.value = false
        _barcode.value = null
    }
}