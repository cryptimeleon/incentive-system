package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.flow.first
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.CryptoMaterial
import org.cryptimeleon.incentive.promotion.Promotion
import timber.log.Timber

class RefreshCryptoDataUseCase(
    val cryptoRepository: ICryptoRepository,
    val promotionRepository: IPromotionRepository
) {
    private lateinit var promotions: List<Promotion>
    private var currentCryptoMaterial: CryptoMaterial? = null
    private var updatedCryptoMaterial: CryptoMaterial? = null

    suspend operator fun invoke() {
        Timber.i("Started")

        // coroutineScope {
        //val loadPromotions = async {
        promotionRepository.reloadPromotions()
        promotions = promotionRepository.promotions.first()
        //}
        //val loadCryptoMaterial = async {
        currentCryptoMaterial = cryptoRepository.cryptoMaterial.first()
        // Loads pp and new keys
        cryptoRepository.refreshCryptoMaterial()
        updatedCryptoMaterial = cryptoRepository.cryptoMaterial.first()
        //}
        //awaitAll(loadCryptoMaterial, loadPromotions)
        //}
        // TODO! new promotions
        if (currentCryptoMaterial == updatedCryptoMaterial && updatedCryptoMaterial != null) {
            // Nothing changed -> Dummy Token
            promotions.forEach {
                Timber.i("Start dummy token for $it")
                cryptoRepository.runIssueJoin(it.promotionParameters, true)
                Timber.i("Retrieved dummy token for $it")
            }
        } else {
            promotions.forEach {
                Timber.i("Start token for $it")
                cryptoRepository.runIssueJoin(it.promotionParameters)
                Timber.i("Retrieved token for $it")
            }
        }
    }
}
