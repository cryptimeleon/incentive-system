package org.cryptimeleon.incentive.app.ui.scan

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
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
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
    val state = MutableLiveData<ScanState>(ScanEmptyState)

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
        if (state.value == ScanEmptyState) {
            state.value = ScanLoadingState(barcode)

            Timber.i(barcode)

            // query basket item from basket service and check if it exists
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    // Query item
                    val basketItem = basketRepository.getBasketItem(barcode)

                    if (basketItem != null) {
                        Timber.i("Item: $basketItem")
                        if (state.value == ScanLoadingState(barcode)) {
                            state.postValue(ScanResultState(basketItem, 1))
                        }
                    } else {
                        basketRepository.refreshShoppingItems()
                        val reloadedBasketItem = basketRepository.getBasketItem(barcode)

                        if (reloadedBasketItem != null) {
                            Timber.i("Item: $reloadedBasketItem")
                            if (state.value == ScanLoadingState(barcode)) {
                                state.postValue(ScanResultState(reloadedBasketItem, 1))
                            } else {
                                Timber.e("Error loading item '$barcode'")
                                if (state.value == ScanLoadingState(barcode)) {
                                    state.postValue(ScanEmptyState)
                                }
                            }
                        }
                    }
                }
                // check if promotions apply or can be applied by adding some more of this item
            }
        }
    }

    /**
     * React on amount changes.
     *
     * @param newAmount the new amount
     */
    fun onAmountChange(newAmount: Int) {
        when (val scanState = state.value) {
            is ScanResultState -> {
                if (newAmount != scanState.count)
                    state.postValue(scanState.copy(count = newAmount))
            }
            else -> {}
        }
    }

    fun onDiscardItem() {
        state.postValue(ScanEmptyState)
    }

    /**
     * Handle the add to basket action.
     * Add amount many of the item to the basket.
     */
    fun onAddToBasket() {
        when (val scanState = state.value) {
            is ScanResultState -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val putItemSuccessful =
                            basketRepository.putItemIntoCurrentBasket(
                                scanState.shoppingItem.id,
                                scanState.count
                            )

                        withContext(Dispatchers.Main) {
                            if (putItemSuccessful) {
                                Toast.makeText(
                                    getApplication(),
                                    "Successfully put ${scanState.shoppingItem.id} to basket.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    getApplication(),
                                    "An error occured when trying to put ${scanState.shoppingItem.id} to basket.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        state.postValue(ScanBlockedState)
                        delay(1000)
                        state.postValue(ScanEmptyState)
                    }
                }
            }
            else -> {}
        }
    }
}

private val locale = Locale.GERMANY
private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

sealed interface ScanState
object ScanEmptyState : ScanState
object ScanBlockedState : ScanState // To avoid direct re-scan
data class ScanLoadingState(val shoppingItemId: String) : ScanState
data class ScanResultState(val shoppingItem: ShoppingItem, val count: Int) : ScanState {
    val priceSingle: String = currencyFormat.format(shoppingItem.price / 100.0)
    val priceTotal: String = currencyFormat.format(shoppingItem.price * count / 100.0)
}
