package org.cryptimeleon.incentive.app.ui.promotion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class PromotionViewModel @Inject constructor(
    cryptoRepository: ICryptoRepository,
    basketRepository: IBasketRepository,
    promotionRepository: IPromotionRepository,
    application: Application
) : AndroidViewModel(application) {
    private val promotionInfoUseCase =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository)

    fun promotionDataFlowFor(promotionId: BigInteger): Flow<PromotionData?> =
        promotionInfoUseCase().map { it.firstOrNull { it.pid == promotionId } }
}
