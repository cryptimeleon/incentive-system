package org.cryptimeleon.incentive.app.domain.usecase

import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository

class ResetAppUseCase(
    private val cryptoRepository: ICryptoRepository,
    private val basketRepository: IBasketRepository,
    private val promotionRepository: IPromotionRepository
) {
    suspend operator fun invoke() {
        cryptoRepository.deleteAll()
        basketRepository.discardCurrentBasket()
        promotionRepository.reloadPromotions()
    }
}
