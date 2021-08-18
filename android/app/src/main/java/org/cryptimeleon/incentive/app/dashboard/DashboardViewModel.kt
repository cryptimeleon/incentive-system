package org.cryptimeleon.incentive.app.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.data.CryptoRepository
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    application: Application
) :
    AndroidViewModel(application) {
    // TODO put this functionality into setup view model since it does not have anything todo with the Dashboard
    val setupFinished = MutableLiveData(false)

    val state: Flow<DashboardState> =
        cryptoRepository.token.map {
            DashboardState(
                it?.let {
                    listOf(PromotionState(count = it.token.points.integer.toInt()))
                } ?: emptyList())
        }
}

data class DashboardState(val promotionStates: List<PromotionState>)

data class PromotionState(
    val title: String = "Main Promotion",
    val description: String = "Earn 1 point for every cent spent.",
    val count: Int = 0
) {

}
