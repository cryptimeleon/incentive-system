package org.cryptimeleon.incentive.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    application: Application,
) : AndroidViewModel(application) {
    val publicParameter = liveData { emit(cryptoRepository.getPublicParameters().toString()) }
    val userSecretKey =
        liveData { emit(cryptoRepository.getUserKeyPair().sk.toString()) }
    val userPublicKey = liveData { emit(cryptoRepository.getUserKeyPair().pk.toString()) }
    val providerPublicKey = liveData { emit(cryptoRepository.getProviderPublicKey().toString()) }
}