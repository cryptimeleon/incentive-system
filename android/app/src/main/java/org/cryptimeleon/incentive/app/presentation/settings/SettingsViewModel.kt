package org.cryptimeleon.incentive.app.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import org.cryptimeleon.incentive.app.data.CryptoRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    application: Application,
) : AndroidViewModel(application) {
    val publicParameter = cryptoRepository.cryptoMaterial.asLiveData().map {
        it!!.pp.toString()
    }
    val userSecretKey = cryptoRepository.cryptoMaterial.asLiveData().map {
        it!!.ukp.sk.toString()
    }
    val userPublicKey = cryptoRepository.cryptoMaterial.asLiveData().map {
        it!!.ukp.pk.toString()
    }
    val providerPublicKey = cryptoRepository.cryptoMaterial.asLiveData().map {
        it!!.ppk.toString()
    }

    val tokens = cryptoRepository.tokens.asLiveData().map { tokens ->
        tokens.map { token -> token.toString() }
    }
}
