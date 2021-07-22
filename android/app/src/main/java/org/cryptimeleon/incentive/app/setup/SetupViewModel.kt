package org.cryptimeleon.incentive.app.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.*
import org.cryptimeleon.incentive.app.network.InfoApi
import org.cryptimeleon.incentive.app.repository.CryptoRepository
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber
import java.util.*

enum class SetupState {
    LOADING_PP,
    LOADING_PROVIDER_PK,
    GENERATING_USER_KEYS,
    FINISHED,
    ERROR
}


class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _setupState = MutableLiveData(SetupState.LOADING_PP)
    private val cryptoRepository = CryptoRepository(application.applicationContext)

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
                val jsonConverter = JSONConverter()
                var invalidateToken = false // This will be set to true if pp or ppk change

                // Repair if previous setup was not finished (e.g. due to hard reset/crash)
                val setupWasFinished = cryptoRepository.getSetupFinished()
                Timber.i("Previous setup finished? %s", setupWasFinished.toString())

                // Invalidate setup finished variable until this setup is finished
                cryptoRepository.setSetupFinished(false)

                // Query public parameters
                _setupState.postValue(SetupState.LOADING_PP)
                Thread.sleep(200)
                Timber.i("Get public parameters")
                val ppResponse = InfoApi.retrofitService.getPublicParameters()

                if (!ppResponse.isSuccessful) {
                    _setupState.postValue(SetupState.ERROR)
                    return@withContext
                }
                Timber.i("PP: %s", ppResponse.body()!!)

                // New public parameters / first start / previous setup failed
                //   -> store pp and generate new user keys
                if (!setupWasFinished || cryptoRepository.getPublicParameters() != ppResponse.body()) {
                    Timber.i("Public parameters changed/were not present. Setting new pp.")
                    val publicParameters =
                        IncentivePublicParameters(jsonConverter.deserialize(ppResponse.body()))
                    val incentiveSystem = IncentiveSystem(publicParameters)
                    cryptoRepository.setPublicParameters(ppResponse.body()!!)

                    _setupState.postValue(SetupState.GENERATING_USER_KEYS)
                    Thread.sleep(200)
                    Timber.i("Generating user keypair")
                    val userKeyPair = incentiveSystem.generateUserKeys()
                    cryptoRepository.setUserPublicKey(jsonConverter.serialize(userKeyPair.pk.representation))
                    cryptoRepository.setUserSecretKey(jsonConverter.serialize(userKeyPair.sk.representation))

                    // If we have a token, we need to delete it since pp and user keys have changed
                    invalidateToken = true
                    Timber.i("Invalidate token since new user keys were generated")
                }

                _setupState.postValue(SetupState.LOADING_PROVIDER_PK)
                Thread.sleep(200)
                // Query provider public key
                Timber.i("Get Provider Provider keys")
                val ppkResponse = InfoApi.retrofitService.getProviderPublicKey()

                if (!ppkResponse.isSuccessful) {
                    _setupState.postValue(SetupState.ERROR)
                    return@withContext
                }
                Timber.i("PPK: %s", ppkResponse.body()!!)

                if (!setupWasFinished || cryptoRepository.getProviderPublicKey() != ppkResponse.body()) {
                    cryptoRepository.setProviderPublicKey(ppkResponse.body()!!)

                    // If we have a token, we need to delete it since the provider public key has changed
                    invalidateToken = true
                    Timber.i("Invalidate token since new provider keys were loaded")
                }

                // Delete token if it is not anymore valid
                if (invalidateToken) {
                    val token = cryptoRepository.getToken()
                    if (token != "") {
                        cryptoRepository.setToken("")
                        _exceptionToast.postValue("Invalid token removed!")
                    }
                }

                // Setup successful, so we can set finished to true and trust this at the next application start
                cryptoRepository.setSetupFinished(true)
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