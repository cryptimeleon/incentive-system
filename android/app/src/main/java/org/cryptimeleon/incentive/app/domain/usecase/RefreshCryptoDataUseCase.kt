package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        val startTime = System.currentTimeMillis()

        coroutineScope {
            val loadPromotions = async {
                promotionRepository.reloadPromotions()
                promotions = promotionRepository.promotions.first()
            }
            val loadCryptoMaterial = async {
                currentCryptoMaterial = cryptoRepository.cryptoMaterial.first()
                cryptoRepository.refreshCryptoMaterial()
                updatedCryptoMaterial = cryptoRepository.cryptoMaterial.first()
            }
            awaitAll(loadCryptoMaterial, loadPromotions)
        }
        // TODO! new promotions?! Maybe only dummy for promotions for which we already have a token!
        // How about a database method? (e.g. insert without replace)
        if (currentCryptoMaterial == updatedCryptoMaterial && updatedCryptoMaterial != null) {
            // Nothing changed -> Dummy Token
            coroutineScope {
                promotions.map {
                    async {
                        Timber.i("Start dummy token for $it")
                        cryptoRepository.runIssueJoin(it.promotionParameters, true)
                        Timber.i("Finished dummy token for $it")
                    }
                }.awaitAll()
            }
        } else {
            coroutineScope {
                promotions.map {
                    async {
                        Timber.i("Start token for $it")
                        cryptoRepository.runIssueJoin(it.promotionParameters)
                        Timber.i("Retrieved token for $it")
                    }
                }.awaitAll()
            }
        }
        val totalMillis = System.currentTimeMillis() - startTime
        Timber.i("Finished withing " + totalMillis + "miliseconds")
    }
}
