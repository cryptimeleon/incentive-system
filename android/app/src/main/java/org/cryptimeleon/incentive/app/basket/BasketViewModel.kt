package org.cryptimeleon.incentive.app.basket

import android.app.Application
import android.icu.text.NumberFormat
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.basket.BasketItemRecyclerViewAdapter.BasketListItem
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.data.network.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val basketApiService: BasketApiService,
    private val cryptoRepository: CryptoRepository,
    private val basketDatabase: BasketDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val locale = Locale.GERMANY
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    private val _basketContent =
        MutableLiveData<List<BasketListItem>>(null)
    val basketContent: LiveData<List<BasketListItem>>
        get() = _basketContent

    private val _basket = MutableLiveData<Basket?>()
    val basket: LiveData<Basket?>
        get() = _basket

    val basketValue = Transformations.map(_basket) {
        it?.let {
            currencyFormat.format(it.value / 100.0)
        }
    }

    val showBasketActionButtons = Transformations.map(_basketContent) {
        (it != null && it.isNotEmpty())
    }

    val visibleIfBasketEmpty = Transformations.map(_basketContent) {
        if (it != null && it.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    val visibleIfBasketNotEmpty = Transformations.map(_basketContent) {
        if (it != null && it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val basket =
                    basketDatabase.basketDatabaseDao()
                        .getBasket() ?: return@withContext
                loadBasketContent(basket.basketId)
            }
        }
    }

    private fun loadBasketContent(basketId: UUID) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val getBasketResponse = basketApiService.getBasketContent(basketId)
                Timber.i(getBasketResponse.toString())
                if (getBasketResponse.isSuccessful) {
                    val basket: Basket = getBasketResponse.body()!!
                    _basket.postValue(basket)

                    val itemsInBasket = ArrayList<BasketListItem>()
                    basket.items.forEach { (id, count) ->
                        val item = basketApiService.getItemById(id)
                        itemsInBasket.add(
                            BasketListItem(
                                item.body()!!,
                                count
                            )
                        )
                    }
                    _basketContent.postValue(itemsInBasket)
                }
            }
        }
    }

    fun setItemCount(itemId: String, count: Int) {
        viewModelScope.launch {
            basket.value?.let {
                withContext(Dispatchers.IO) {
                    val response = if (count <= 0) {
                        basketApiService.removeItemFromBasket(
                            it.basketId,
                            itemId
                        )
                    } else {
                        basketApiService.putItemToBasket(
                            BasketItem(
                                it.basketId,
                                count,
                                itemId
                            )
                        )
                    }
                    if (response.isSuccessful) {
                        loadBasketContent(it.basketId)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                getApplication(),
                                "Could not update item $itemId",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                }
            }
        }
    }

    fun newBasket(delete: Boolean = true) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val basketId = basket.value!!.basketId
                basketDatabase.basketDatabaseDao()
                    .setActive(false, basketId)
                if (delete) basketApiService.deleteBasket(basketId)

                _basket.postValue(null)
                _basketContent.postValue(ArrayList())

                val basketResponse = basketApiService.getNewBasket()
                if (basketResponse.isSuccessful) {
                    val basket = org.cryptimeleon.incentive.app.data.database.basket.Basket(
                        basketResponse.body()!!,
                        true
                    )
                    basketDatabase.basketDatabaseDao()
                        .insertBasket(basket)
                    loadBasketContent(basket.basketId)
                }
            }
        }
    }

    fun payAndRedeem() {
        // TODO continue working on this functionality
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val basketId = basket.value!!.basketId

                // Pay basket
                val payResponse =
                    basketApiService.payBasket(PayBody(basketId, basket.value!!.value))
                if (!payResponse.isSuccessful) {
                    Timber.e("An exception occured when trying to pay the basket with id $basketId: $payResponse")
                    Toast.makeText(
                        getApplication(), "Could not pay basket!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@withContext
                }

                basketDatabase.basketDatabaseDao()
                    .setActive(false, basketId)

                // Redeem basket
                cryptoRepository.runCreditEarn(
                    basketId,
                    basket.value!!.value
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(), "Successfully paid and redeemed basket!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                newBasket()
            }
        }
    }
}

