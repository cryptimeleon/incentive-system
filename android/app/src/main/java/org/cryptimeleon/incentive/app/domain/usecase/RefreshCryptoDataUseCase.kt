package org.cryptimeleon.incentive.app.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.app.domain.model.CryptoMaterial
import org.cryptimeleon.incentive.promotion.Promotion

class RefreshCryptoDataUseCase(
    val cryptoRepository: ICryptoRepository,
    val promotionRepository: IPromotionRepository,
    val basketRepository: IBasketRepository,
    val preferencesRepository: IPreferencesRepository
) {
    private lateinit var promotions: List<Promotion>
    private var currentCryptoMaterial: CryptoMaterial? = null
    private var updatedCryptoMaterial: CryptoMaterial? = null

    suspend operator fun invoke() {
        loadPromotionsAndCryptoMaterialParallel()
        joinAllPromotions(forceStoreTokens = !isDummy())
    }

    private suspend fun loadPromotionsAndCryptoMaterialParallel() {
        coroutineScope {
            val loadPromotions = async {
                promotionRepository.reloadPromotions()
                promotions = promotionRepository.promotions.first()
            }
            val loadCryptoMaterial = async {
                currentCryptoMaterial = cryptoRepository.cryptoMaterial.first()
                val userDataForRegistration = preferencesRepository.userDataPreferencesFlow.first()
                cryptoRepository.refreshCryptoMaterial(userDataForRegistration)
                updatedCryptoMaterial = cryptoRepository.cryptoMaterial.first()
            }
            val loadShoppingItems = async {
                basketRepository.refreshShoppingItems()
            }
            val loadRewardItems = async {
                basketRepository.refreshRewardItems()
            }
            awaitAll(loadCryptoMaterial, loadPromotions, loadShoppingItems, loadRewardItems)
        }
    }

    private fun isDummy() =
        currentCryptoMaterial == updatedCryptoMaterial && updatedCryptoMaterial != null


    private suspend fun joinAllPromotions(forceStoreTokens: Boolean) {
        coroutineScope {
            promotions.map {
                async {
                    cryptoRepository.runIssueJoin(
                        it.promotionParameters,
                        replaceIfPresent = forceStoreTokens
                    )
                }
            }.awaitAll()
        }
    }
}
