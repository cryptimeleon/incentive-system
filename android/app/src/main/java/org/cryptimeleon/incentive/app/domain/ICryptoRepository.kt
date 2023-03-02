package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.*
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
    suspend fun refreshCryptoMaterial(userDataForRegistration: String)
    suspend fun runIssueJoin(
        promotionParameters: PromotionParameters,
        replaceIfPresent: Boolean = true
    )


    suspend fun putToken(promotionParameters: PromotionParameters, token: Token)
    suspend fun deleteAll()
    suspend fun sendTokenUpdatesBatchToStore(
        bulkRequestStoreDto: BulkRequestStoreDto
    )

    suspend fun retrieveTokenUpdatesBatchStoreResults(basketId: UUID): BulkResultStoreDto
    suspend fun sendTokenUpdatesBatchToProvider(bulkRequestProviderDto: BulkRequestProviderDto): BulkResultsProviderDto
}

class DSException : Exception()
class PayRedeemException(val code: Int, val msg: String) : Exception()
