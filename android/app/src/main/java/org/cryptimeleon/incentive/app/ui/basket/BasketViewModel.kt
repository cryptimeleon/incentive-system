package org.cryptimeleon.incentive.app.ui.basket

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.util.SLE
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val basketRepository: IBasketRepository,
    application: Application
) : AndroidViewModel(application) {

    val basket: Flow<SLE<Basket>> = basketRepository.basket.map {
        Timber.i("Basket ${it}, ${it?.items}")
        if (it == null) {
            SLE.Error("Basket is null!")
        } else {
            SLE.Success(it)
        }
    }

    fun setItemCount(itemId: String, count: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (!basketRepository.putItemIntoCurrentBasket(itemId, count)) {
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
