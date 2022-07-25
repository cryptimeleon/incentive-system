package org.cryptimeleon.incentive.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    application: Application,
) : AndroidViewModel(application) {

    val publicParameter = cryptoRepository.cryptoMaterial.map {
        it!!.pp.toString()
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), "")

    val userSecretKey = cryptoRepository.cryptoMaterial.map {
        it!!.ukp.sk.toString()
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), "")

    val userPublicKey = cryptoRepository.cryptoMaterial.map {
        it!!.ukp.pk.toString()
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), "")

    val providerPublicKey = cryptoRepository.cryptoMaterial.map {
        it!!.ppk.toString()
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), "")

    val tokens = cryptoRepository.tokens.map { tokens ->
        tokens.map { token -> token.toString() }
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), emptyList())
}
