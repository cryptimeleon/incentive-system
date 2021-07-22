package org.cryptimeleon.incentive.app.scan

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.network.BasketApi
import org.cryptimeleon.incentive.app.network.Item
import timber.log.Timber

/**
 * ViewModel for the ScanFragment.
 * Handles detected barcodes, queries the product, processes the product and allows adding the basket.
 * In the future, it could find related promotions based on the current basket content, tokens and
 * scanned item, e.g. add three of these to get one for free etc.
 */
class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentBarcode = MutableLiveData<String>()
    val item = MutableLiveData<Item>()
    val showItem = MutableLiveData(false)

    /**
     * Handle a new barcode being scanned.
     *  0. Do some checks to ensure items are only analyzed once upon showing item information
     *  1. Query item
     *  2. Ensure item result
     *  3. Show item
     *
     *  @param barcode the scanned barcode
     */
    fun setBarcode(barcode: String) {
        // Check if barcode already registered or being displayed
        if (showItem.value == true || _currentBarcode.value == barcode || barcode == "") {
            return
        }

        _currentBarcode.value = barcode
        Timber.i(barcode)

        // query basket item from basket service and check if it exists
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Query item
                val itemResponse = BasketApi.retrofitService.getItemById(barcode)

                if (itemResponse.isSuccessful) {
                    Timber.i(itemResponse.body().toString())
                    item.postValue(itemResponse.body())
                    showItem.postValue(true)
                } else {
                    // Handle error case
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Scanned item not found\n($barcode)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Timber.i("Item with id $barcode not found")
                }
            }
            // check if promotions apply or can be applied by adding some more of this item
        }
    }

    /**
     * Handle the case that the dialog closing the item was closed.
     */
    fun showItemFinished() {
        showItem.value = false
        item.value = null
        // Reset barcode to allow rescanning the same item after closing the dialog
        _currentBarcode.value = ""
    }
}