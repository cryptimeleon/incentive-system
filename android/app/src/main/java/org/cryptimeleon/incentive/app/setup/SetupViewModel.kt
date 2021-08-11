package org.cryptimeleon.incentive.app.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.cryptimeleon.incentive.app.database.basket.Basket
import org.cryptimeleon.incentive.app.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.database.crypto.CryptoDatabase
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository
import org.cryptimeleon.incentive.app.network.BasketApiService
import org.cryptimeleon.incentive.app.network.InfoApiService
import org.cryptimeleon.incentive.app.network.IssueJoinApiService
import timber.log.Timber
import java.util.*
import javax.inject.Inject

enum class SetupState {
    LOADING_PP,
    LOADING_PROVIDER_PK,
    GENERATING_USER_KEYS,
    FINISHED,
    ERROR,
    ISSUE_JOIN,
    SETUP_BASKET
}


@HiltViewModel
class SetupViewModel @Inject constructor(
    private val basketApiService: BasketApiService,
    private val infoApiService: InfoApiService,
    private val issueJoinApiService: IssueJoinApiService,
    private val cryptoDatabase: CryptoDatabase,
    application: Application,
) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _setupState = MutableLiveData(SetupState.LOADING_PP)
    private val basketDatabase = BasketDatabase.getInstance(application.applicationContext)

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
            SetupState.LOADING_PP -> "Loading Public Parameters"
            SetupState.LOADING_PROVIDER_PK -> "Loading Provider Public Key"
            SetupState.GENERATING_USER_KEYS -> "Generating a fresh Keypair"
            SetupState.FINISHED -> "Finished!"
            SetupState.ERROR -> "An error occurred!"
            SetupState.ISSUE_JOIN -> "Retrieving new token"
            SetupState.SETUP_BASKET -> "Setting up basket!"
        }
    }

    init {
        Timber.i("Init SetupViewModel")
        setup()
    }

    private fun setup() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                _setupState.postValue(SetupState.LOADING_PP)
                val ppResponse = infoApiService.getPublicParameters()
                if (!ppResponse.isSuccessful) {
                    _setupState.postValue(SetupState.ERROR)
                    return@withContext
                }
                Timber.i("PP: %s", ppResponse.body()!!)

                _setupState.postValue(SetupState.LOADING_PROVIDER_PK)
                // Query provider public key
                Timber.i("Get Provider Provider keys")
                val ppkResponse = infoApiService.getProviderPublicKey()

                if (!ppkResponse.isSuccessful) {
                    _setupState.postValue(SetupState.ERROR)
                    return@withContext
                }
                Timber.i("PPK: %s", ppkResponse.body()!!)

                Timber.i("Run issue-join protocol for new (dummy-) token, setup crypto repository")
                _setupState.postValue(SetupState.ISSUE_JOIN)

                // TODO put this into the repository
                CryptoRepository.setup(
                    ppResponse.body()!!,
                    ppkResponse.body()!!,
                    issueJoinApiService,
                    cryptoDatabase.cryptoDatabaseDao(),
                )

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