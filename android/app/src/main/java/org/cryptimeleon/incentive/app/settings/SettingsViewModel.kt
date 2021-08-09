package org.cryptimeleon.incentive.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val cryptoRepository = CryptoRepository.getInstance(application.applicationContext)

    val publicParameter = liveData { emit(cryptoRepository.getPublicParameters().toString()) }
    val userSecretKey =
        liveData { emit(cryptoRepository.getUserKeyPair().sk.toString()) }
    val userPublicKey = liveData { emit(cryptoRepository.getUserKeyPair().pk.toString()) }
    val providerPublicKey = liveData { emit(cryptoRepository.getProviderPublicKey().toString()) }
}