package org.cryptimeleon.incentive.app.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    application: Application
) :
    AndroidViewModel(application) {
    // TODO put this functionality into setup view model since it does not have anything todo with the Dashboard
    val setupFinished = MutableLiveData(false)
    val labelTokenPoints =
        cryptoRepository.tokens.asLiveData().map { "${it[0].points} Points" }
}