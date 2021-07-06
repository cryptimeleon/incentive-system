package org.cryptimeleon.incentive.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cryptimeleon.incentive.app.repository.crypto.CryptoRepository

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val cryptoRepository = CryptoRepository.getInstance(application.applicationContext)

    // TODO LiveData depends on lazy data. How to fix this?

    private val _userSecretKey = MutableLiveData(cryptoRepository.userKeyPair.sk.toString())
    val userSecretKey: LiveData<String>
        get() = _userSecretKey

    private val _userPublicKey = MutableLiveData(cryptoRepository.userKeyPair.pk.toString())
    val userPublicKey: LiveData<String>
        get() = _userPublicKey

    private val _providerPublicKey = MutableLiveData(cryptoRepository.providerPublicKey.toString())
    val providerPublicKey: LiveData<String>
        get() = _providerPublicKey

    private val _publicParameters = MutableLiveData(cryptoRepository.publicParameters.toString())
    val publicParameter: LiveData<String>
        get() = _publicParameters
}