package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.model.*
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import java.util.*

class FakeCryptoRepository(
    private val pp: IncentivePublicParameters,
    private val providerKeyPair: ProviderKeyPair,
    private val userKeyPair: UserKeyPair
) : ICryptoRepository {
    private val _tokens = MutableStateFlow(emptyList<Token>())
    override val tokens
        get() = _tokens
    private val _cryptoMaterial: MutableStateFlow<CryptoMaterial?> = MutableStateFlow(null)
    override val cryptoMaterial: Flow<CryptoMaterial?>
        get() = _cryptoMaterial
    private val incentiveSystem = IncentiveSystem(pp)

    override suspend fun refreshCryptoMaterial(userDataForRegistration: String) {
        _cryptoMaterial.value = CryptoMaterial(pp, providerKeyPair.pk, userKeyPair)
    }

    override suspend fun runIssueJoin(
        promotionParameters: PromotionParameters,
        replaceIfPresent: Boolean
    ) {
        val token = joinPromotion(promotionParameters)!!
        if (replaceIfPresent) {
            _tokens.value = _tokens.value
                .filter { it.promotionId != promotionParameters.promotionId }
                .plus(token)
        } else if (_tokens.value.any { it.promotionId != promotionParameters.promotionId }) {
            _tokens.value = _tokens.value.plus(token)
        } else {
            // Dummy token
        }
    }

    private fun joinPromotion(promotionParameters: PromotionParameters): Token? {
        val generateIssueJoinOutput = incentiveSystem.generateJoinRequest(
            providerKeyPair.pk,
            userKeyPair,
        )
        val joinResponse = incentiveSystem.generateJoinRequestResponse(
            promotionParameters,
            providerKeyPair,
            generateIssueJoinOutput.joinRequest
        )
        return incentiveSystem.handleJoinRequestResponse(
            promotionParameters,
            providerKeyPair.pk,
            generateIssueJoinOutput,
            joinResponse
        )
    }

    override suspend fun putToken(promotionParameters: PromotionParameters, token: Token) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun sendTokenUpdatesBatchToStore(
        bulkRequestStoreDto: BulkRequestStoreDto
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveTokenUpdatesBatchStoreResults(basketId: UUID): BulkResultStoreDto {
        TODO("Not yet implemented")
    }

    override suspend fun sendTokenUpdatesBatchToProvider(bulkRequestProviderDto: BulkRequestProviderDto): BulkResultsProviderDto {
        TODO("Not yet implemented")
    }
}
