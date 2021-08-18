package org.cryptimeleon.incentive.app.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.network.Item
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the ScanFragment.
 * Handles detected barcodes, queries the product, processes the product and allows adding the basket.
 * In the future, it could find related promotions based on the current basket content, tokens and
 * scanned item, e.g. add three of these to get one for free etc.
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val basketRepository: BasketRepository,
    application: Application
) :
    AndroidViewModel(application) {
    val item = MutableLiveData<Item>()
    val showItem = MutableLiveData(false)
    private val canScanItem = MutableLiveData(true)

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
        if (showItem.value == true || barcode == "" || canScanItem.value == false) {
            return
        }

        Timber.i(barcode)

        // query basket item from basket service and check if it exists
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Query item
                val basketItem = basketRepository.getBasketItem(barcode) ?: return@withContext

                Timber.i("Item: $basketItem")
                if (showItem.value == false) {
                    item.postValue(basketItem)
                    showItem.postValue(true)
                    canScanItem.postValue(false)
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
        viewModelScope.launch {
            // Wait one second between closing the dialog and scanning the next item to avoid
            // accidentally scanning the same item again
            delay(1000L)
            canScanItem.value = true
        }
    }
}