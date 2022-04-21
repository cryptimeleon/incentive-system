package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem
import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.BulkResponseDto
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol
import org.cryptimeleon.math.serialization.converter.JSONConverter
import retrofit2.Response
import java.util.*

class FakeCryptoApiService(
    private val pp: IncentivePublicParameters,
    private val providerKeyPair: ProviderKeyPair,
    private val promotionParametersList: List<PromotionParameters>
) : CryptoApiService {
    private val incentiveSystem = IncentiveSystem(pp)
    private val jsonConverter = JSONConverter()

    override suspend fun runIssueJoin(
        joinRequest: String,
        promotionId: String,
        publicKey: String
    ): Response<String> {
        val promotionParameters = promotionParametersList.find {
            it.promotionId.toString() == promotionId
        }!!
        return runIssueJoin(joinRequest, promotionParameters, publicKey)
    }

    override suspend fun sendTokenUpdatesBatch(
        basketId: UUID,
        bulkRequestDto: BulkRequestDto
    ): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveTokenUpdatesResults(basketId: UUID): Response<BulkResponseDto> {
        TODO("Not yet implemented")
    }

    companion object FakeCryptoApiServiceHelper {

    }

    fun runIssueJoin(
        joinRequest: String,
        promotionParameters: PromotionParameters,
        publicKey: String,
    ): Response<String> {
        val userPublicKey = UserPublicKey(jsonConverter.deserialize(publicKey), pp.bg.g1)
        val joinResponse = incentiveSystem.generateJoinRequestResponse(
            promotionParameters,
            providerKeyPair,
            userPublicKey.upk,
            JoinRequest(
                jsonConverter.deserialize(joinRequest),
                pp,
                userPublicKey,
                FiatShamirProofSystem(
                    CommitmentWellformednessProtocol(pp, providerKeyPair.pk)
                )
            )
        )
        return Response.success(jsonConverter.serialize(joinResponse.representation))
    }
}
