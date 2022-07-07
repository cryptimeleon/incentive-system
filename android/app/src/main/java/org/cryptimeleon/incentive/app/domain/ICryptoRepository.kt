package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.BulkResponseDto
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
    @Throws(RefreshCryptoMaterialException::class)
    suspend fun refreshCryptoMaterial()
    suspend fun runIssueJoin(promotionParameters: PromotionParameters, dummy: Boolean = false)

    // Results are stored at the server's side until basket is payed
    suspend fun sendTokenUpdatesBatch(basketId: UUID, bulkRequestDto: BulkRequestDto)
    suspend fun retrieveTokenUpdatesResults(basketId: UUID): BulkResponseDto
    suspend fun putToken(promotionParameters: PromotionParameters, token: Token)
    suspend fun deleteAll()
}
