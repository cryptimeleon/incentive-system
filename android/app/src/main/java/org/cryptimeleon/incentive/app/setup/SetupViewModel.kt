package org.cryptimeleon.incentive.app.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.*
import org.cryptimeleon.incentive.app.network.InfoApi
import org.cryptimeleon.incentive.app.repository.crypto.CryptoRepository
import timber.log.Timber
import java.util.*

enum class SetupState {
    LOADING_PP,
    LOADING_PROVIDER_PK,
    GENERATING_USER_KEYS,
    FINISHED,
    ERROR
}


class SetupViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _setupState = MutableLiveData(SetupState.LOADING_PP)
    private val cryptoRepository = CryptoRepository.getInstance(application.applicationContext)

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
                val ppResponse = InfoApi.retrofitService.getPublicParameters()
                if (!ppResponse.isSuccessful) {
                    _setupState.postValue(SetupState.ERROR)
                    return@withContext
                }
                Timber.i("PP: %s", ppResponse.body()!!)

                _setupState.postValue(SetupState.LOADING_PROVIDER_PK)
                // Query provider public key
                Timber.i("Get Provider Provider keys")
                val ppkResponse = InfoApi.retrofitService.getProviderPublicKey()

                if (!ppkResponse.isSuccessful) {
                    _setupState.postValue(SetupState.ERROR)
                    return@withContext
                }
                Timber.i("PPK: %s", ppkResponse.body()!!)

                cryptoRepository.setup(ppResponse.body()!!, ppkResponse.body()!!)

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