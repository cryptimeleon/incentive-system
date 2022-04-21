package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.BulkResponseDto
import org.cryptimeleon.incentive.app.domain.model.CryptoMaterial
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

    override suspend fun refreshCryptoMaterial(): Boolean {
        _cryptoMaterial.value = CryptoMaterial(pp, providerKeyPair.pk, userKeyPair)
        return true
    }

    override suspend fun runIssueJoin(promotionParameters: PromotionParameters, dummy: Boolean) {
        if (!dummy) {
            val incentiveSystem = IncentiveSystem(pp)
            val joinRequest = incentiveSystem.generateJoinRequest(providerKeyPair.pk, userKeyPair, promotionParameters)
            val joinResponse = incentiveSystem.generateJoinRequestResponse(
                promotionParameters,
                providerKeyPair,
                userKeyPair.pk.upk,
                joinRequest
            )
            val token = incentiveSystem.handleJoinRequestResponse(
                promotionParameters,
                providerKeyPair.pk,
                userKeyPair,
                joinRequest,
                joinResponse
            )
            _tokens.value =
                _tokens.value.filter { it.promotionId != promotionParameters.promotionId }
                    .plus(token)
        }
    }

    override suspend fun sendTokenUpdatesBatch(basketId: UUID, bulkRequestDto: BulkRequestDto) {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveTokenUpdatesResults(basketId: UUID): BulkResponseDto {
        TODO("Not yet implemented")
    }

    override suspend fun putToken(promotionParameters: PromotionParameters, token: Token) {
        TODO("Not yet implemented")
    }
}
