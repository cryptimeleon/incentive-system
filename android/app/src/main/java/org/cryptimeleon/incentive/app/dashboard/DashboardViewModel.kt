package org.cryptimeleon.incentive.app.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    val setupFinished = MutableLiveData(false)
    private val cryptoRepository = CryptoRepository.getInstance(getApplication())
    val labelTokenPoints =
        cryptoRepository.tokens.asLiveData().map { "${it[0].points} Points" }
}