package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDao
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoMaterialEntity
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoTokenEntity
import org.cryptimeleon.incentive.app.data.network.ProviderApiService
import org.cryptimeleon.incentive.app.data.network.InfoApiService
import org.cryptimeleon.incentive.app.data.network.StoreApiService
import org.cryptimeleon.incentive.app.domain.DSException
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.PayRedeemException
import org.cryptimeleon.incentive.app.domain.RefreshCryptoMaterialException
import org.cryptimeleon.incentive.app.domain.model.*
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.IncentiveSystemRestorer
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.JoinResponse
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber
import java.util.*

/**
 * Repository that handles the crypto database, provides cached deserialized crypto objects and
 * methods for running the protocols.
 */
class CryptoRepository(
    private val infoApiService: InfoApiService,
    private val providerApiService: ProviderApiService,
    private val cryptoDao: CryptoDao,
    private val storeApiService: StoreApiService,
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


    override suspend fun runIssueJoin(
        promotionParameters: PromotionParameters,
        replaceIfPresent: Boolean
    ) {
        val cryptoMaterial = cryptoMaterial.first()!!
        val pp = cryptoMaterial.pp
        val providerPublicKey = cryptoMaterial.ppk
        val userKeyPair = cryptoMaterial.ukp
        val incentiveSystem = IncentiveSystem(pp)

        val generateIssueJoinOutput =
            incentiveSystem.generateJoinRequest(providerPublicKey, userKeyPair)
        val joinResponse = providerApiService.runIssueJoin(
            jsonConverter.serialize(generateIssueJoinOutput.joinRequest.representation),
            promotionParameters.promotionId.toString()
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
            generateIssueJoinOutput,
            JoinResponse(jsonConverter.deserialize(joinResponse.body()), pp)
        )
        if (replaceIfPresent) {
            cryptoDao.insertToken(toCryptoTokenEntity(token))
        } else {
            cryptoDao.insertTokenIfNotPresent(toCryptoTokenEntity(token))
        }
    }

    @Deprecated("Old api")
    override suspend fun sendTokenUpdatesBatch(
        basketId: UUID,
        bulkRequestDto: BulkRequestDto
    ) {
        val response = providerApiService.sendTokenUpdatesBatch(basketId, bulkRequestDto)
        if (!response.isSuccessful) {
            Timber.e(response.raw().toString())
            if (response.code() == 418) {
                throw DSException()
            } else {
                throw PayRedeemException(response.code(), response.errorBody()?.string() ?: "")
            }
        }
    }

    @Deprecated("Old api")
    override suspend fun retrieveTokenUpdatesResults(basketId: UUID): BulkResponseDto {
        val response = providerApiService.retrieveTokenUpdatesResults(basketId)
        if (!response.isSuccessful || response.body() == null) {
            Timber.e(response.raw().toString())
            throw PayRedeemException(response.code(), response.errorBody()?.string() ?: "")
        }
        return response.body()!!
    }

    override suspend fun sendTokenUpdatesBatchToStore(
        basketId: UUID,
        bulkRequestStoreDto: BulkRequestStoreDto
    ) {
        val response = storeApiService.sendBulkRequest(bulkRequestStoreDto)
        if (!response.isSuccessful) {
            Timber.e(response.raw().toString())
            if (response.code() == 418) {
                // TODO add error codes once implemented
                throw DSException()
            } else {
                throw PayRedeemException(response.code(), response.errorBody()?.string() ?: "")
            }
        }
    }

    override suspend fun retrieveTokenUpdatesBatchStoreResults(basketId: UUID): BulkResultStoreDto {
        val response = storeApiService.retrieveBulkResponse(basketId)
        if (!response.isSuccessful || response.body() == null) {
            Timber.e(response.raw().toString())
            throw PayRedeemException(response.code(), response.errorBody()?.string() ?: "")
        }
        return response.body()!!
    }

    override suspend fun sendTokenUpdatesBatchToProvider(bulkRequestProviderDto: BulkRequestProviderDto): BulkResultsProviderDto {
        val response = providerApiService.bulkRequest(bulkRequestProviderDto)
        if (!response.isSuccessful || response.body() == null) {
            Timber.e(response.raw().toString())
            if (response.code() == 418) {
                // TODO add error codes once implemented
                throw DSException()
            } else {
                throw PayRedeemException(response.code(), response.errorBody()?.string() ?: "")
            }
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

    override suspend fun deleteAll() {
        cryptoDao.deleteTokens()
        cryptoDao.deleteCryptoMaterial()
    }

    override suspend fun refreshCryptoMaterial(userDataForRegistration: String) {
        val oldSerializedCryptoAsset = cryptoDao.observeCryptoMaterial().first()
        val (remotePP: String, remotePPK: String) = queryRemoteCryptoMaterial()

        if (needToResetCryptoData(oldSerializedCryptoAsset, remotePP, remotePPK)) {
            Timber.d("Deleting all crypto data since PP or PPK have changed")
            deleteAll()
            generateAndStoreNewCryptoAssets(remotePP, remotePPK, userDataForRegistration)
        }
    }

    private suspend fun generateAndStoreNewCryptoAssets(
        remotePP: String,
        remotePPK: String,
        userDataForRegistration: String
    ) {
        // Setup
        val pp = IncentivePublicParameters(jsonConverter.deserialize(remotePP))
        val incentiveSystem = IncentiveSystem(pp)
        val providerPublicKey = ProviderPublicKey(jsonConverter.deserialize(remotePPK), pp)
        val userPreKeyPair = incentiveSystem.generateUserPreKeyPair()

        // Registration
        val registrationCouponResponse = storeApiService.retrieveRegistrationCouponFor(jsonConverter.serialize(userPreKeyPair.pk.representation), userDataForRegistration)
        if (!registrationCouponResponse.isSuccessful) throw java.lang.RuntimeException("Registration at Store failed" + registrationCouponResponse.code())

        val serializedRegistrationCoupon = registrationCouponResponse.body() ?: throw RuntimeException("Registration at Store unsuccessful")
        val registrationCoupon = RegistrationCoupon(jsonConverter.deserialize(serializedRegistrationCoupon), IncentiveSystemRestorer(pp))
        assert(incentiveSystem.verifyRegistrationCoupon(registrationCoupon) { true })

        val signatureResponse =
            providerApiService.retrieveRegistrationSignatureFor(serializedRegistrationCoupon)
        if (!signatureResponse.isSuccessful) {
            throw RuntimeException("Signature Request failed!")
        }
        val signature =
            pp.spsEq.restoreSignature(jsonConverter.deserialize(signatureResponse.body()))
        assert(incentiveSystem.verifyRegistrationToken(providerPublicKey, signature, registrationCoupon))


        // Store everything
        val userKeyPair = UserKeyPair(userPreKeyPair, signature)
        val newCryptoAsset = CryptoMaterial(pp, providerPublicKey, userKeyPair)
        cryptoDao.insertCryptoMaterial(toSerializedCryptoAsset(newCryptoAsset))
    }

    private fun needToResetCryptoData(
        oldSerializedCryptoAsset: CryptoMaterialEntity?,
        remotePP: String,
        remotePPK: String
    ) = oldSerializedCryptoAsset == null ||
            oldSerializedCryptoAsset.serializedPublicParameters != remotePP ||
            oldSerializedCryptoAsset.serializedProviderPublicKey != remotePPK

    private suspend fun queryRemoteCryptoMaterial(): Pair<String, String> {
        try {
            val ppResponse = infoApiService.getPublicParameters()
            val ppkResponse = infoApiService.getProviderPublicKey()

            if (!ppResponse.isSuccessful || ppResponse.body() == "") {
                Timber.e(ppResponse.errorBody().toString())
                throw RefreshCryptoMaterialException(ppResponse.errorBody().toString())
            }

            if (!ppkResponse.isSuccessful || ppkResponse.body() == "") {
                Timber.e(ppkResponse.errorBody().toString())
                throw RefreshCryptoMaterialException(ppkResponse.errorBody().toString())
            }

            val remotePP = ppResponse.body().toString()
            val remotePPK = ppkResponse.body().toString()
            return Pair(remotePP, remotePPK)
        } catch (e: Exception) {
            Timber.e(e)
            throw RefreshCryptoMaterialException(e.toString())
        }
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
                pp
            )
            val upk = UserPublicKey(
                jsonConverter.deserialize(cryptoMaterialEntity.serializedUserPublicKey),
                pp
            )
            val usk = UserSecretKey(
                jsonConverter.deserialize(cryptoMaterialEntity.serializedUserSecretKey),
                pp
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
