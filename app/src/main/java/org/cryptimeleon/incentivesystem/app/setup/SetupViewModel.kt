package org.cryptimeleon.incentivesystem.app.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.*
import org.cryptimeleon.incentivesystem.app.repository.CryptoRepository
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem
import org.cryptimeleon.incentivesystem.cryptoprotocol.Setup
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber

enum class SetupState {
    LOADING_PP,
    LOADING_PROVIDER_PK,
    GENERATING_USER_KEYS,
    FINISHED
}

const val SECURITY_PARAMETER = 128
val BILINEAR_GROUP = Setup.BilinearGroupChoice.Debug

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _setupState = MutableLiveData(SetupState.LOADING_PP)
    private val cryptoRepository = CryptoRepository(application.applicationContext)

    private val _navigateToInfo = MutableLiveData(false)
    val navigateToInfo: LiveData<Boolean>
        get() = _navigateToInfo

    val feedbackText: LiveData<String> = Transformations.map(_setupState) {
        Timber.i("State: $it")
        when (it!!) {
            SetupState.LOADING_PP -> "Loading Public Parameters"
            SetupState.LOADING_PROVIDER_PK -> "Loading Provider Public Key"
            SetupState.GENERATING_USER_KEYS -> "Generating a fresh Keypair"
            SetupState.FINISHED -> "Finished!"
        }
    }

    init {
        if (!cryptoRepository.getSetupFinished()) {
            setup()
        } else {
            _setupState.value = SetupState.FINISHED
            _navigateToInfo.value = true
        }
    }

    private fun setup() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                // Setup cryptographic assets (some will be retrieved via http request later)
                Timber.i("Generating public parameters")
                val jsonConverter = JSONConverter()

                val incentivePublicParameters =
                    Setup.trustedSetup(SECURITY_PARAMETER, BILINEAR_GROUP)
                cryptoRepository.setPublicParameters(
                    jsonConverter.serialize(
                        incentivePublicParameters.representation
                    )
                )
                _setupState.postValue(SetupState.LOADING_PROVIDER_PK)

                val incentiveSystem = IncentiveSystem(incentivePublicParameters)

                Timber.i("Provider Provider keys")
                val providerKeyPair = incentiveSystem.generateProviderKeys()
                cryptoRepository.setProviderPublicKey(jsonConverter.serialize(providerKeyPair.pk.representation))
                cryptoRepository.setProviderSecretKey(jsonConverter.serialize(providerKeyPair.sk.representation))
                _setupState.postValue(SetupState.GENERATING_USER_KEYS)

                Timber.i("Generating User keys")
                val userKeyPair = incentiveSystem.generateUserKeys()
                cryptoRepository.setUserPublicKey(jsonConverter.serialize(userKeyPair.pk.representation))
                cryptoRepository.setUserSecretKey(jsonConverter.serialize(userKeyPair.sk.representation))

                cryptoRepository.setSetupFinished(true)
                _setupState.postValue(SetupState.FINISHED)
                Thread.sleep(200)
            }
            _navigateToInfo.value = true
        }
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