package org.cryptimeleon.incentive.app.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import timber.log.Timber
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
    private val basketRepository: BasketRepository,
    private val promotionRepository: PromotionRepository,
    application: Application,
) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _setupState = MutableLiveData(SetupState.LOADING_CRYPTO_MATERIAL)

    private val _navigateToInfo = MutableLiveData(false)
    val navigateToInfo: LiveData<Boolean>
        get() = _navigateToInfo

    val inErrorState: LiveData<Boolean> = Transformations.map(_setupState) {
        it == SetupState.ERROR
    }

    val feedbackText: LiveData<String> = Transformations.map(_setupState) {
        Timber.i("State: $it")
        when (it!!) {
            SetupState.FINISHED -> "Finished!"
            SetupState.ERROR -> "An error occurred!"
            SetupState.ISSUE_JOIN -> "Retrieving new token"
            SetupState.SETUP_BASKET -> "Setting up basket"
            SetupState.LOADING_CRYPTO_MATERIAL -> "Loading crypto material"
        }
    }

    init {
        Timber.i("Init SetupViewModel")
    }

    fun startSetup() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                // Load promotions TODO make this more efficient
                promotionRepository.reloadPromotions()
                val promotions = promotionRepository.promotions.first()

                // Load pp and provider keys
                Timber.i("Load crypto material and generate keys if needed")
                _setupState.postValue(SetupState.LOADING_CRYPTO_MATERIAL)
                val storeDummy = !cryptoRepository.refreshCryptoMaterial()

                // Load (dummy-) token
                Timber.i("Run issue-join protocol for new (dummy-) token, setup crypto repository")
                _setupState.postValue(SetupState.ISSUE_JOIN)
                promotions.forEach {
                    cryptoRepository.runIssueJoin(it.promotionParameters, storeDummy)
                }

                // Ensure there is an active basket
                Timber.i("Setup basket")
                _setupState.postValue(SetupState.SETUP_BASKET)
                if (basketRepository.ensureActiveBasket()) {
                    _setupState.postValue(SetupState.FINISHED)
                } else {
                    _setupState.postValue(SetupState.ERROR)
                }

                delay(200)
                _navigateToInfo.postValue(true)
            }
        }
    }

    override fun onCleared() {
        viewModelJob.cancel()
        Timber.i("Coroutine canceled")
        super.onCleared()
    }
}
