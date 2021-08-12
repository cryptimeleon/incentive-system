package org.cryptimeleon.incentive.app.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.database.basket.Basket
import org.cryptimeleon.incentive.app.data.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import timber.log.Timber
import java.util.*
import javax.inject.Inject

enum class SetupState {
    FINISHED,
    ERROR,
    ISSUE_JOIN,
    SETUP_BASKET,
    LOADING_CRYPTO_MATERIAL
}


@HiltViewModel
class SetupViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val basketApiService: BasketApiService,
    private val basketDatabase: BasketDatabase,
    application: Application,
) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _setupState = MutableLiveData(SetupState.LOADING_CRYPTO_MATERIAL)

    private val _navigateToInfo = MutableLiveData(false)
    val navigateToInfo: LiveData<Boolean>
        get() = _navigateToInfo

    private val _exceptionToast = MutableLiveData("")
    val exceptionToast: LiveData<String>
        get() = _exceptionToast

    val inErrorState: LiveData<Boolean> = Transformations.map(_setupState) {
        it == SetupState.ERROR
    }

    val feedbackText: LiveData<String> = Transformations.map(_setupState) {
        Timber.i("State: $it")
        when (it!!) {
            SetupState.FINISHED -> "Finished!"
            SetupState.ERROR -> "An error occurred!"
            SetupState.ISSUE_JOIN -> "Retrieving new token"
            SetupState.SETUP_BASKET -> "Setting up basket!"
            SetupState.LOADING_CRYPTO_MATERIAL -> "Loading crypto material!"
        }
    }

    init {
        Timber.i("Init SetupViewModel")
        setup()
    }

    private fun setup() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                _setupState.postValue(SetupState.LOADING_CRYPTO_MATERIAL)

                val storeDummyToken = cryptoRepository.refreshCryptoMaterial()

                Timber.i("Run issue-join protocol for new (dummy-) token, setup crypto repository")
                _setupState.postValue(SetupState.ISSUE_JOIN)

                cryptoRepository.runIssueJoin(!storeDummyToken)

                Timber.i("Setup basket")
                _setupState.postValue(SetupState.SETUP_BASKET)

                var basket: Basket? = basketDatabase.basketDatabaseDao().getBasket()
                Timber.i("Old basket $basket")

                // TODO Check if basket is known to the basket service
                if (basket == null || !basket.isActive) {
                    val basketResponse = basketApiService.getNewBasket()
                    if (basketResponse.isSuccessful) {
                        basket = Basket(basketResponse.body()!!, true)

                        // Make sure all other baskets are set to inactive
                        basketDatabase.basketDatabaseDao().setAllInactive()
                        basketDatabase.basketDatabaseDao().insertBasket(basket)
                    } else {
                        _setupState.postValue(SetupState.ERROR)
                        return@withContext
                    }
                }

                Timber.i("Using basket $basket")

                _setupState.postValue(SetupState.FINISHED)
                Thread.sleep(200)
                _navigateToInfo.postValue(true)
            }
        }
    }

    fun toastShown() {
        _exceptionToast.value = ""
    }

    fun navigateToInfoFinished() {
        _navigateToInfo.value = false
    }

    override fun onCleared() {
        viewModelJob.cancel()
        Timber.i("Coroutine canceled")
        super.onCleared()
    }
}