package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDao
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoMaterial
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoToken
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoUtil
import org.cryptimeleon.incentive.app.data.network.CreditEarnApiService
import org.cryptimeleon.incentive.app.data.network.InfoApiService
import org.cryptimeleon.incentive.app.data.network.IssueJoinApiService
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse
import org.cryptimeleon.math.serialization.converter.JSONConverter
import org.cryptimeleon.math.structures.cartesian.Vector
import timber.log.Timber
import java.math.BigInteger
import java.util.UUID

/**
 * Repository that handles the crypto database, provides cached deserialized crypto objects and
 * methods for running the protocols.
 *
 * TODO add caching (e.g. by hashing the serialized assets and putting the deserialized object into a hashmap)
 */
class CryptoRepository(
    private val creditEarnApiService: CreditEarnApiService,
    private val infoApiService: InfoApiService,
    private val issueJoinApiService: IssueJoinApiService,
    private val cryptoDao: CryptoDao,
) {
    private val jsonConverter = JSONConverter()

    // Tokens change
    // TODO Make sure pp (cryptoMaterial.first()) are valid when token changes
    val token: Flow<CryptoToken?> = cryptoDao.observeToken().map {
        val cryptoMaterial = observeCryptoMaterial().first()
        if (cryptoMaterial == null || it == null) {
            null
        } else {
            CryptoUtil.toCryptoToken(it, cryptoMaterial.pp)
        }
    }

    fun observeCryptoMaterial(): Flow<CryptoMaterial?> = cryptoDao.observeCryptoMaterial().map {
        if (it == null) {
            null
        } else {
            CryptoUtil.fromSerializedCryptoAsset(it)
        }
    }

    suspend fun runIssueJoin(dummy: Boolean = false) {
        val cryptoMaterial = observeCryptoMaterial().first()!!
        val pp = cryptoMaterial.pp
        val incentiveSystem = cryptoMaterial.incentiveSystem
        val providerPublicKey = cryptoMaterial.ppk
        val userKeyPair = cryptoMaterial.ukp
        val promotionParameters = incentiveSystem.legacyPromotionParameters()

        val joinRequest = incentiveSystem.generateJoinRequest(providerPublicKey, userKeyPair)
        val joinResponse = issueJoinApiService.runIssueJoin(
            jsonConverter.serialize(joinRequest.representation),
            jsonConverter.serialize(userKeyPair.pk.representation)
        )

        val token = incentiveSystem.handleJoinRequestResponse(
            promotionParameters,
            providerPublicKey,
            userKeyPair,
            joinRequest,
            JoinResponse(jsonConverter.deserialize(joinResponse.body()), pp)
        )
        if (!dummy) {
            cryptoDao.insertToken(
                CryptoUtil.fromCryptoToken(
                    CryptoToken(token, 1, cryptoMaterial.id)
                )
            )
        }
    }

    suspend fun runCreditEarn(basketId: UUID, basketValue: Int) {
        val cryptoMaterial = observeCryptoMaterial().first()!!
        val cryptoToken = token.first()!!
        val token = cryptoToken.token
        val pp = cryptoMaterial.pp
        val incentiveSystem = cryptoMaterial.incentiveSystem
        val providerPublicKey = cryptoMaterial.ppk
        val userKeyPair = cryptoMaterial.ukp
        val promotionParameters = incentiveSystem.legacyPromotionParameters()

        val earnRequest =
            incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair)
        val earnResponse = creditEarnApiService.runCreditEarn(
            basketId,
            jsonConverter.serialize(earnRequest.representation)
        )

        Timber.i("Earn response $earnResponse")

        // The basket service computes the value in the backend, so no need to send it over the wire
        val newToken = incentiveSystem.handleEarnRequestResponse(
            promotionParameters,
            earnRequest,
            SPSEQSignature(
                jsonConverter.deserialize(earnResponse.body()),
                pp.bg.g1,
                pp.bg.g2
            ),
            Vector.of(BigInteger.valueOf(basketValue.toLong())),
            token,
            providerPublicKey,
            userKeyPair
        )

        // Update token in database
        // Will be nicer when handling multiple tokens
        cryptoDao.deleteAllTokens()
        cryptoDao.insertToken(
            CryptoUtil.fromCryptoToken(
                CryptoToken(
                    newToken,
                    cryptoToken.promotionId,
                    cryptoToken.cryptoMaterialId
                )
            )
        )
        Timber.i("Added new token $newToken to database")
    }

    /**
     * Refresh the crypto material by querying the info service. Deletes all old tokens if crypto
     * material has changed.
     *
     * @return true if crypto material has changed
     */
    suspend fun refreshCryptoMaterial(): Boolean {
        val oldSerializedCryptoAsset = cryptoDao.observeSerializedCryptoMaterial().first()

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
                incentiveSystem,
                1
            )

            cryptoDao.insertAsset(CryptoUtil.toSerializedCryptoAsset(newCryptoAsset))
            cryptoDao.deleteAllTokens()
            // TODO invalidate all tokens with cascade
            return true
        }

        return false
    }
}
