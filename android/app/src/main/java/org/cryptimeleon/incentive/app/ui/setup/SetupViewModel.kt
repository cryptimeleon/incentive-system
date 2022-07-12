package org.cryptimeleon.incentive.app.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
