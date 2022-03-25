package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDao
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoMaterialEntity
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoTokenEntity
import org.cryptimeleon.incentive.app.data.network.CryptoApiService
import org.cryptimeleon.incentive.app.data.network.InfoApiService
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.model.BulkRequestDto
import org.cryptimeleon.incentive.app.domain.model.BulkResponseDto
import org.cryptimeleon.incentive.app.domain.model.CryptoMaterial
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber
import java.util.*

/**
 * Repository that handles the crypto database, provides cached deserialized crypto objects and
 * methods for running the protocols.
 */
class CryptoRepository(
    private val infoApiService: InfoApiService,
    private val cryptoApiService: CryptoApiService,
    private val cryptoDao: CryptoDao,
) : ICryptoRepository {
    private val jsonConverter = JSONConverter()

    override val tokens: Flow<List<Token>>
        get() = cryptoDao.observeTokens().map {
            val cryptoMaterial = cryptoMaterial.first()
            if (cryptoMaterial != null) {
                it.map { cryptoTokenEntity -> toCryptoToken(cryptoTokenEntity, cryptoMaterial.pp) }
            } else {
                emptyList()
            }
        }.flowOn(Dispatchers.Default)

    override val cryptoMaterial: Flow<CryptoMaterial?>
        get() = cryptoDao.observeCryptoMaterial().map {
            it?.let { it1 -> toCryptoMaterial(it1) }
        }.flowOn(Dispatchers.Default)


    override suspend fun runIssueJoin(promotionParameters: PromotionParameters, dummy: Boolean) {
        val cryptoMaterial = cryptoMaterial.first()!!
        val pp = cryptoMaterial.pp
        val providerPublicKey = cryptoMaterial.ppk
        val userKeyPair = cryptoMaterial.ukp
        val incentiveSystem = IncentiveSystem(pp)

        val joinRequest =
            incentiveSystem.generateJoinRequest(providerPublicKey, userKeyPair, promotionParameters)
        val joinResponse = cryptoApiService.runIssueJoin(
            jsonConverter.serialize(joinRequest.representation),
            promotionParameters.promotionId.toString(),
            jsonConverter.serialize(userKeyPair.pk.representation)
        )

        if (!joinResponse.isSuccessful) {
            Timber.e(joinResponse.raw().toString())
            throw RuntimeException(
                "Join not successful"
            )
        }


        val token = incentiveSystem.handleJoinRequestResponse(
            promotionParameters,
            providerPublicKey,
            userKeyPair,
            joinRequest,
            JoinResponse(jsonConverter.deserialize(joinResponse.body()), pp)
        )
        if (!dummy) {
            cryptoDao.insertToken(toCryptoTokenEntity(token))
        }
    }

    override suspend fun sendTokenUpdatesBatch(
        basketId: UUID,
        bulkRequestDto: BulkRequestDto
    ) {
        val response = cryptoApiService.sendTokenUpdatesBatch(basketId, bulkRequestDto)
        if (!response.isSuccessful) {
            Timber.e(response.raw().toString())
            throw RuntimeException(response.errorBody().toString())
        }
    }

    override suspend fun retrieveTokenUpdatesResults(basketId: UUID): BulkResponseDto {
        val response = cryptoApiService.retrieveTokenUpdatesResults(basketId)
        if (!response.isSuccessful || response.body() == null) {
            Timber.e(response.raw().toString())
            throw RuntimeException(response.errorBody().toString())
        }
        return response.body()!!
    }

    override suspend fun putToken(promotionParameters: PromotionParameters, token: Token) {
        cryptoDao.insertToken(
            CryptoTokenEntity(
                promotionParameters.promotionId.toInt(),
                jsonConverter.serialize(token.representation)
            )
        )
    }

    override suspend fun refreshCryptoMaterial(): Boolean {
        val oldSerializedCryptoAsset = cryptoDao.observeCryptoMaterial().first()

        val ppResponse = infoApiService.getPublicParameters()
        val ppkResponse = infoApiService.getProviderPublicKey()

        if (!ppResponse.isSuccessful || ppResponse.body() == "") {
            Timber.e(ppResponse.errorBody().toString())
            return false
        }

        if (!ppkResponse.isSuccessful || ppkResponse.body() == "") {
            Timber.e(ppkResponse.errorBody().toString())
            return false
        }

        if (oldSerializedCryptoAsset == null ||
            oldSerializedCryptoAsset.serializedPublicParameters != ppResponse.body() ||
            oldSerializedCryptoAsset.serializedProviderPublicKey != ppkResponse.body()
        ) {
            // First query or new pp -> invalidate all
            Timber.i("Updating crypto assets")
            val jsonConverter = JSONConverter()
            val publicParameters =
                IncentivePublicParameters(jsonConverter.deserialize(ppResponse.body()))
            val incentiveSystem = IncentiveSystem(publicParameters)
            val providerPublicKey = ProviderPublicKey(
                jsonConverter.deserialize(ppkResponse.body()),
                publicParameters.spsEq,
                publicParameters.bg.g1
            )
            val userKeyPair = incentiveSystem.generateUserKeys()
            val newCryptoAsset = CryptoMaterial(
                publicParameters,
                providerPublicKey,
                userKeyPair,
            )

            cryptoDao.insertCryptoMaterial(toSerializedCryptoAsset(newCryptoAsset))
            return true
        }

        return false
    }

    /**
     * Utility for serializing tokens to database objects and vice versa.
     * This is not a Converter since it requires the public parameters as additional resources.
     */
    companion object Converter {
        private val jsonConverter = JSONConverter()

        fun toCryptoTokenEntity(token: Token): CryptoTokenEntity = CryptoTokenEntity(
            token.promotionId.toInt(),
            jsonConverter.serialize(token.representation)
        )

        fun toCryptoToken(
            cryptoTokenEntity: CryptoTokenEntity,
            pp: IncentivePublicParameters
        ): Token =
            Token(jsonConverter.deserialize(cryptoTokenEntity.serializedToken), pp)

        fun toCryptoMaterial(cryptoMaterialEntity: CryptoMaterialEntity): CryptoMaterial {
            val pp =
                IncentivePublicParameters(jsonConverter.deserialize(cryptoMaterialEntity.serializedPublicParameters))
            val ppk = ProviderPublicKey(
                jsonConverter.deserialize(cryptoMaterialEntity.serializedProviderPublicKey),
                pp.spsEq,
                pp.bg.g1
            )
            val upk = UserPublicKey(
                jsonConverter.deserialize(cryptoMaterialEntity.serializedUserPublicKey),
                pp.bg.g1
            )
            val usk = UserSecretKey(
                jsonConverter.deserialize(cryptoMaterialEntity.serializedUserSecretKey),
                pp.bg.zn,
                pp.prfToZn
            )
            return CryptoMaterial(
                pp,
                ppk,
                UserKeyPair(upk, usk),
            )
        }

        fun toSerializedCryptoAsset(cryptoMaterial: CryptoMaterial): CryptoMaterialEntity =
            CryptoMaterialEntity(
                jsonConverter.serialize(cryptoMaterial.pp.representation),
                jsonConverter.serialize(cryptoMaterial.ppk.representation),
                jsonConverter.serialize(cryptoMaterial.ukp.pk.representation),
                jsonConverter.serialize(cryptoMaterial.ukp.sk.representation),
            )
    }
}
