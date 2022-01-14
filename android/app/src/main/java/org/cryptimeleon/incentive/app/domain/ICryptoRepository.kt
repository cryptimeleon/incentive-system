package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.CryptoMaterial
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.Token
import java.util.*

interface ICryptoRepository {
    // Flow of a list of all tokens. Can be identified by their promotion id.
    val tokens: Flow<List<Token>>

    // Flow of the crypto material
    val cryptoMaterial: Flow<CryptoMaterial?>

    /**
     * Refresh the crypto material by querying the info service.
     *
     * @return true if crypto material has changed
     */
    suspend fun refreshCryptoMaterial(): Boolean
    suspend fun runIssueJoin(promotionParameters: PromotionParameters, dummy: Boolean = false)
    suspend fun runCreditEarn(
        basketId: UUID,
        promotionParameters: PromotionParameters,
        basketValue: Int
    )
}