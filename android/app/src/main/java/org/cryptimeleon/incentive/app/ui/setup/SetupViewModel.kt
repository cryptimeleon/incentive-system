package org.cryptimeleon.incentive.app.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.RefreshCryptoDataUseCase
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
    cryptoRepository: CryptoRepository,
    private val basketRepository: BasketRepository,
    promotionRepository: PromotionRepository,
    application: Application,
) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val refreshCryptoDataUseCase =
        RefreshCryptoDataUseCase(cryptoRepository, promotionRepository)
    private val _setupState = MutableLiveData(SetupState.LOADING_CRYPTO_MATERIAL)

    private val _navigateToInfo = MutableLiveData(false)
    val navigateToInfo: LiveData<Boolean>
        get() = _navigateToInfo

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

    fun startSetup() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Timber.i("1")
                    refreshCryptoDataUseCase()
                    Timber.i("2")
                    basketRepository.ensureActiveBasket()
                    Timber.i("3")
                    _navigateToInfo.postValue(true)
                    Timber.i("4")
                } catch (e: Exception) {
                    Timber.e(e)
                    _navigateToInfo.postValue(true)
                }
            }
        }
    }

    override fun onCleared() {
        viewModelJob.cancel()
        Timber.i("Coroutine canceled")
        super.onCleared()
    }
}
