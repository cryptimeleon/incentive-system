package org.cryptimeleon.incentive.app.basket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.network.BasketApi
import org.cryptimeleon.incentive.app.repository.basket.BasketDatabase
import timber.log.Timber

class BasketViewModel(application: Application) : AndroidViewModel(application) {
    private val _basketContent = MutableLiveData<String>()
    val basketContent: LiveData<String>
        get() = _basketContent

    fun loadBasketContent() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val basketId =
                    BasketDatabase.getInstance(getApplication()).basketDatabaseDao()
                        .getBasket().basketId
                val getBasketResponse = BasketApi.retrofitService.getBasketContent(basketId)
                Timber.i(getBasketResponse.toString())
                if (getBasketResponse.isSuccessful) {
                    _basketContent.postValue(getBasketResponse.body().toString())
                }
            }
        }
    }
}
