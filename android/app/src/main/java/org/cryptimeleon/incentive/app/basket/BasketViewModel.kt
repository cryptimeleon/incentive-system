package org.cryptimeleon.incentive.app.basket

import android.app.Application
import android.icu.text.NumberFormat
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.basket.BasketItemRecyclerViewAdapter.BasketListItem
import org.cryptimeleon.incentive.app.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository
import org.cryptimeleon.incentive.app.network.Basket
import org.cryptimeleon.incentive.app.network.BasketApi
import org.cryptimeleon.incentive.app.network.BasketItem
import org.cryptimeleon.incentive.app.network.PayBody
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class BasketViewModel(application: Application) : AndroidViewModel(application) {

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
                val basketId =
                    BasketDatabase.getInstance(getApplication()).basketDatabaseDao()
                        .getBasket().basketId
                loadBasketContent(basketId)
            }
        }
    }

    private fun loadBasketContent(basketId: UUID) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val getBasketResponse = BasketApi.retrofitService.getBasketContent(basketId)
                Timber.i(getBasketResponse.toString())
                if (getBasketResponse.isSuccessful) {
                    val basket: Basket = getBasketResponse.body()!!
                    _basket.postValue(basket)

                    val itemsInBasket = ArrayList<BasketListItem>()
                    basket.items.forEach { (id, count) ->
                        val item = BasketApi.retrofitService.getItemById(id)
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
                        BasketApi.retrofitService.removeItemFromBasket(
                            it.basketId,
                            itemId
                        )
                    } else {
                        BasketApi.retrofitService.putItemToBasket(
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
                BasketDatabase.getInstance(getApplication()).basketDatabaseDao()
                    .setActive(false, basketId)
                if (delete) BasketApi.retrofitService.deleteBasket(basketId)

                _basket.postValue(null)
                _basketContent.postValue(ArrayList())

                val basketResponse = BasketApi.retrofitService.getNewBasket()
                if (basketResponse.isSuccessful) {
                    val basket = org.cryptimeleon.incentive.app.database.basket.Basket(
                        basketResponse.body()!!,
                        true
                    )
                    BasketDatabase.getInstance(getApplication()).basketDatabaseDao()
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
                    BasketApi.retrofitService.payBasket(PayBody(basketId, basket.value!!.value))
                if (!payResponse.isSuccessful) {
                    Timber.e("An exception occured when trying to pay the basket with id $basketId: $payResponse")
                    Toast.makeText(
                        getApplication(), "Could not pay basket!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@withContext
                }

                BasketDatabase.getInstance(getApplication()).basketDatabaseDao()
                    .setActive(false, basketId)

                // Redeem basket
                CryptoRepository.getInstance(getApplication()).runCreditEarn(
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

