package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoMaterial
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoToken
import java.util.*

interface ICryptoRepository {
    // Tokens change
    // TODO Make sure pp (cryptoMaterial.first()) are valid when token changes
    val token: Flow<CryptoToken?>
    fun observeCryptoMaterial(): Flow<CryptoMaterial?>

    suspend fun runIssueJoin(dummy: Boolean = false)

    suspend fun runCreditEarn(basketId: UUID, basketValue: Int)

    /**
     * Refresh the crypto material by querying the info service. Deletes all old tokens if crypto
     * material has changed.
     *
     * @return true if crypto material has changed
     */
    suspend fun refreshCryptoMaterial(): Boolean
}