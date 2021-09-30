package org.cryptimeleon.incentive.app.scan

import android.app.Application
import android.widget.Toast
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
import java.text.NumberFormat
import java.util.Locale
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
    val state = MutableLiveData<ScanState?>(null)
    var allowScan = true

    init {
        Timber.i("ScanViewModel created")
    }

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
        if (!allowScan || barcode == "" || state.value != null) {
            return
        }

        Timber.i(barcode)

        // query basket item from basket service and check if it exists
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Query item
                val basketItem = basketRepository.getBasketItem(barcode) ?: return@withContext

                Timber.i("Item: $basketItem")
                if (state.value == null) {
                    state.postValue(ScanState(basketItem, 1))
                    allowScan = false
                }
            }
            // check if promotions apply or can be applied by adding some more of this item
        }
    }

    /**
     * React on amount changes.
     *
     * @param newAmount the new amount
     */
    fun onAmountChange(newAmount: Int) {
        state.value?.let {
            if (newAmount != it.count)
                state.postValue(it.copy(count = newAmount))
        }
    }

    fun onDiscardItem() {
        state.postValue(null)
        allowScan = true
    }

    /**
     * Handle the add to basket action.
     * Add amount many of the item to the basket.
     */
    fun onAddToBasket() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                state.value?.let {
                    val putItemSuccessful =
                        basketRepository.putItemIntoCurrentBasket(
                            it.count,
                            it.item.id
                        )

                    withContext(Dispatchers.Main) {
                        if (putItemSuccessful) {
                            Toast.makeText(
                                getApplication(),
                                "Successfully put ${it.item.id} to basket.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                getApplication(),
                                "An error occured when trying to put ${it.item.id} to basket.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    state.postValue(null)
                    delay(1000L)
                    allowScan = true
                }
            }
        }
    }
}

private val locale = Locale.GERMANY
private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

data class ScanState(val item: Item, val count: Int) {
    val priceSingle: String = currencyFormat.format(item.price / 100.0)
    val priceTotal: String = currencyFormat.format(item.price * count / 100.0)
}
