package org.cryptimeleon.incentive.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    promotionRepository: IPromotionRepository,
    basketRepository: IBasketRepository,
    application: Application
) :
    AndroidViewModel(application) {

    val promotionDataListFlow: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()
}
