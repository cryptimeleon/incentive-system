package org.cryptimeleon.incentive.app.ui.rewards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.domain.model.Earn
import org.cryptimeleon.incentive.app.domain.model.None
import org.cryptimeleon.incentive.app.domain.model.ZKP
import org.cryptimeleon.incentive.app.domain.usecase.EarnTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.NoTokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.PromotionData
import org.cryptimeleon.incentive.app.domain.usecase.PromotionInfoUseCase
import org.cryptimeleon.incentive.app.domain.usecase.TokenUpdate
import org.cryptimeleon.incentive.app.domain.usecase.ZkpTokenUpdate
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    cryptoRepository: CryptoRepository,
    basketRepository: BasketRepository,
    private val promotionRepository: PromotionRepository,
    application: Application
) : AndroidViewModel(application) {

    val promotionDataListFlow: Flow<List<PromotionData>> =
        PromotionInfoUseCase(promotionRepository, cryptoRepository, basketRepository).invoke()

    fun setUpdateChoice(promotionId: BigInteger, tokenUpdate: TokenUpdate) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val userUpdateChoice = when (tokenUpdate) {
                    is NoTokenUpdate -> None
                    is EarnTokenUpdate -> Earn
                    is ZkpTokenUpdate -> ZKP(tokenUpdate.zkpUpdateId)
                    else -> {
                        throw RuntimeException("Unknown token update $tokenUpdate")
                    }
                }
                promotionRepository.putUserUpdateChoice(promotionId, userUpdateChoice)
            }
        }
    }

}
