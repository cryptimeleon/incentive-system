package org.cryptimeleon.incentive.app.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoToken
import org.cryptimeleon.incentive.promotion.promotions.Promotion
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    promotionRepository: PromotionRepository,
    application: Application
) :
    AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                promotionRepository.reloadPromotions()
            }
        }
    }

    val state: StateFlow<DashboardState> = promotionRepository.promotions
        .combine(cryptoRepository.token) { a: List<Promotion>, b: CryptoToken? ->
            DashboardState(
                a.map {
                    PromotionState(
                        title = it.promotionParameters.promotionId.toString(),
                        description = it.rewards.toString(),
                        count = if (b != null && b.promotionId == it.promotionParameters.promotionId.toInt()
                        ) b.token.points.get(0).asInteger().toInt() else 0
                    )
                }
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState(emptyList())
        )

}

data class DashboardState(val promotionStates: List<PromotionState>)

data class PromotionState(
    val title: String = "Main Promotion",
    val description: String = "Earn 1 point for every cent spent.",
    val count: Int = 0
)
