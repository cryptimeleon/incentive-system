package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem
import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.BulkResponseDto
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.JoinRequest
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol
import org.cryptimeleon.math.serialization.converter.JSONConverter
import org.cryptimeleon.math.structures.groups.GroupElement
import retrofit2.Response
import java.util.*

class FakeCryptoApiService(
    private val pp: IncentivePublicParameters,
    private val providerKeyPair: ProviderKeyPair,
    private val promotionParametersList: List<PromotionParameters>
) : CryptoApiService {
    private val incentiveSystem = IncentiveSystem(pp)
    private val jsonConverter = JSONConverter()

    override suspend fun retrieveRegistrationSignatureFor(serializedRegistrationCoupon: String): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveGenesisSignatureFor(serializedPublicKey: String): Response<String> {
        val publicKey: GroupElement =
            pp.bg.g1.restoreElement(jsonConverter.deserialize(serializedPublicKey))
        val signature = pp.spsEq.sign(providerKeyPair.sk.genesisSpsEqSk, publicKey, pp.w)
        return Response.success(jsonConverter.serialize(signature.representation))
    }

    override suspend fun runIssueJoin(
        joinRequest: String,
        promotionId: String
    ): Response<String> {
        val promotionParameters = promotionParametersList.find {
            it.promotionId.toString() == promotionId
        }!!
        return runIssueJoin(joinRequest, promotionParameters)
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
    ): Response<String> {
        val joinResponse = incentiveSystem.generateJoinRequestResponse(
            promotionParameters,
            providerKeyPair,
            JoinRequest(
                jsonConverter.deserialize(joinRequest),
                pp,
                FiatShamirProofSystem(
                    CommitmentWellformednessProtocol(pp, providerKeyPair.pk)
                )
            )
        )
        return Response.success(jsonConverter.serialize(joinResponse.representation))
    }
}
