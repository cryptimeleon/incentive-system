package org.cryptimeleon.incentive.app.ui.scan

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.ShoppingItem
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

const val NUMBER_SEARCH_ITEMS = 3

/**
 * ViewModel for the ScanFragment.
 * Handles detected barcodes, queries the product, processes the product and allows adding the basket.
 * In the future, it could find related promotions based on the current basket content, tokens and
 * scanned item, e.g. add three of these to get one for free etc.
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val basketRepository: IBasketRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<ScanState>(ScanEmptyState)
    val state: LiveData<ScanState>
        get() = _state

    private val _itemFilter = MutableStateFlow("")
    val itemFilter: StateFlow<String>
        get() = _itemFilter
    val itemsFlow = basketRepository.shoppingItems.combine(itemFilter) { items, filter ->
        items.filter {
            it.title.lowercase().contains(filter.lowercase())
        }.take(NUMBER_SEARCH_ITEMS)
    }

    init {
        Timber.i("ScanViewModel created")
    }

    fun setFilter(filter: String) {
        _itemFilter.value = filter
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
        // Reset filter in case item was added via search
        _itemFilter.value = ""
        // do this on main dispatcher!
        if (_state.value == ScanEmptyState) {
            _state.value = ScanLoadingState(barcode)
        } else {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Check if barcode already registered or being displayed
                _state.postValue(ScanLoadingState(barcode))
                Timber.i("Loading item with code $barcode")
                delay(100)

                // TODO! trailing zeros in barcode?

                // Search for item, first locally and then by reloading
                var shoppingItem: ShoppingItem?
                shoppingItem = basketRepository.getBasketItem(barcode)
                if (shoppingItem == null) {
                    // Try refreshing shoppingItems
                    basketRepository.refreshShoppingItems()
                    shoppingItem = basketRepository.getBasketItem(barcode)
                }

                Timber.i("Found item $shoppingItem for barcode $barcode")

                if (shoppingItem != null) {
                    _state.postValue(ScanResultState(shoppingItem, 1))
                } else {
                    _state.postValue(ScanBlockedState)
                    delay(300)
                    _state.postValue(ScanEmptyState)
                }
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
                    _state.postValue(scanState.copy(count = newAmount))
            }
            else -> {}
        }
    }

    fun onDiscardItem() {
        _state.postValue(ScanEmptyState)
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
                        _state.postValue(ScanBlockedState)
                        delay(1000)
                        _state.postValue(ScanEmptyState)
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
