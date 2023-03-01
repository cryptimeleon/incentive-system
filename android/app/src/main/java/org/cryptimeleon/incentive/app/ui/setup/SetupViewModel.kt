package org.cryptimeleon.incentive.app.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.RefreshCryptoDataUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository,
    promotionRepository: IPromotionRepository,
    preferencesRepository: IPreferencesRepository,
    application: Application,
) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val refreshCryptoDataUseCase =
        RefreshCryptoDataUseCase(
            cryptoRepository,
            promotionRepository,
            basketRepository,
            preferencesRepository
        )

    private val _navigateToInfo = MutableLiveData(false)
    val navigateToInfo: LiveData<Boolean>
        get() = _navigateToInfo

    fun startSetup() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    refreshCryptoDataUseCase()
                    basketRepository.ensureActiveBasket()
                    _navigateToInfo.postValue(true)
                } catch (e: Exception) {
                    Timber.e(e)
                    _navigateToInfo.postValue(true)
                }
            }
        }
    }

    override fun onCleared() {
        viewModelJob.cancel()
        super.onCleared()
    }
}
