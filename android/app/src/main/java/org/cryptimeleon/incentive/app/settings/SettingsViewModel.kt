package org.cryptimeleon.incentive.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val cryptoRepository = CryptoRepository.getInstance(application.applicationContext)

    private val _userSecretKey = MutableLiveData<String>()
    val userSecretKey: LiveData<String>
        get() = _userSecretKey

    private val _userPublicKey = MutableLiveData<String>()
    val userPublicKey: LiveData<String>
        get() = _userPublicKey

    private val _providerPublicKey = MutableLiveData<String>()
    val providerPublicKey: LiveData<String>
        get() = _providerPublicKey

    private val _publicParameters = MutableLiveData<String>()
    val publicParameter: LiveData<String>
        get() = _publicParameters

    init {
        _userPublicKey.value = cryptoRepository.userKeyPair.pk.toString()
        _userSecretKey.value =
            cryptoRepository.userKeyPair.sk.toString()
        _providerPublicKey.value = cryptoRepository.providerPublicKey.toString()
        _publicParameters.value =
            cryptoRepository.publicParameters.toString()
    }
}