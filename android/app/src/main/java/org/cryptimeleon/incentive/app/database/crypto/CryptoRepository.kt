package org.cryptimeleon.incentive.app.database.crypto

import android.content.Context
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature
import org.cryptimeleon.incentive.app.network.CreditEarnApi
import org.cryptimeleon.incentive.app.network.IssueJoinApi
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber
import java.math.BigInteger
import java.util.*
import org.cryptimeleon.incentive.crypto.model.Token as CryptoModelToken

const val PP = "PUBLIC_PARAMETERS"
const val USER_PUBLIC_KEY = "USER_PUBLIC_KEY"
const val USER_SECRET_KEY = "USER_SECRET_KEY"
const val PROVIDER_PUBLIC_KEY = "PROVIDER_PUBLIC_KEY"
const val SETUP_FINISHED = "SETUP_FINISHED"
const val TRUE = "TRUE"
const val FALSE = "FALSE" // not really needed, but looks nicer

/**
 * Repository that handles the crypto database, provides cached deserialized crypto objects and
 * methods for running the protocols.
 */
class CryptoRepository(context: Context) {
    private val jsonConverter = JSONConverter()
    private val cryptoDao = CryptoDatabase.getInstance(context).cryptoDatabaseDao()

    // We assume these do not change once the app is started
    lateinit var publicParameters: IncentivePublicParameters
    lateinit var incentiveSystem: IncentiveSystem
    lateinit var userKeyPair: UserKeyPair
    lateinit var providerPublicKey: ProviderPublicKey

    // Tokens do change
    lateinit var token: CryptoModelToken

    /**
     * Initialization algorithm that takes freshly queried pp and provider public key,
     * sets up the incentive system, deletes invalid tokens/keys and stores everything to the database
     *
     * @param serializedPP the current public parameters serialized to a string
     * @param serializedProviderPublicKey the current server public key serialized to a string
     */
    suspend fun setup(serializedPP: String, serializedProviderPublicKey: String) {
        var invalidateToken = false // This will be set to true if pp or ppk change

        // Repair if previous setup was not finished (e.g. due to hard reset/crash)
        val setupWasFinished =
            (cryptoDao.getAssetByName(SETUP_FINISHED) == TRUE)
        Timber.i("Previous setup finished? %s", setupWasFinished.toString())

        // Invalidate setup finished variable until this setup is finished
        cryptoDao.insertAsset(SerializedCryptoAsset(SETUP_FINISHED, FALSE))

        // New public parameters / first start / previous setup failed
        //   -> store pp and generate new user keys
        val oldPP = cryptoDao.getAssetByName(PP)

        publicParameters =
            IncentivePublicParameters(jsonConverter.deserialize(serializedPP))
        incentiveSystem = IncentiveSystem(publicParameters)

        if (!setupWasFinished || oldPP != serializedPP) {
            Timber.i("Public parameters changed/were not present. Setting new pp.")
            cryptoDao.insertAsset(SerializedCryptoAsset(PP, serializedPP))

            Timber.i("Generating user keypair")
            userKeyPair = incentiveSystem.generateUserKeys()
            cryptoDao.insertAsset(
                SerializedCryptoAsset(
                    USER_PUBLIC_KEY,
                    jsonConverter.serialize(userKeyPair.pk.representation)
                )
            )
            cryptoDao.insertAsset(
                SerializedCryptoAsset(
                    USER_SECRET_KEY,
                    jsonConverter.serialize(userKeyPair.sk.representation)
                )
            )

            // If we have a token, we need to delete it since pp and user keys have changed
            invalidateToken = true
            Timber.i("Invalidate token since new user keys were generated")
        } else {
            Timber.i("Loading user keys from database")

            userKeyPair = UserKeyPair(
                UserPublicKey(
                    jsonConverter.deserialize(cryptoDao.getAssetByName(USER_PUBLIC_KEY)),
                    publicParameters.bg.g1
                ),
                UserSecretKey(
                    jsonConverter.deserialize(cryptoDao.getAssetByName(USER_SECRET_KEY)),
                    publicParameters.bg.zn,
                    publicParameters.prfToZn
                ),
            )
        }

        val oldProviderPublicKey = cryptoDao.getAssetByName(PROVIDER_PUBLIC_KEY)
        if (!setupWasFinished || oldProviderPublicKey != serializedProviderPublicKey) {
            cryptoDao.insertAsset(
                SerializedCryptoAsset(
                    PROVIDER_PUBLIC_KEY,
                    serializedProviderPublicKey
                )
            )
            providerPublicKey = ProviderPublicKey(
                jsonConverter.deserialize(serializedProviderPublicKey),
                publicParameters.spsEq,
                publicParameters.bg.g1
            )

            // If we have a token, we need to delete it since the provider public key has changed
            invalidateToken = true
            Timber.i("Invalidate token since new provider keys were loaded")
        } else {
            providerPublicKey = ProviderPublicKey(
                jsonConverter.deserialize(cryptoDao.getAssetByName(PROVIDER_PUBLIC_KEY)),
                publicParameters.spsEq,
                publicParameters.bg.g1
            )
        }

        // Query dummy token
        val joinRequest = incentiveSystem.generateJoinRequest(providerPublicKey, userKeyPair)
        val joinResponse = IssueJoinApi.retrofitService.runIssueJoin(
            jsonConverter.serialize(joinRequest.representation),
            jsonConverter.serialize(userKeyPair.pk.representation)
        )

        val dummyToken = incentiveSystem.handleJoinRequestResponse(
            providerPublicKey,
            userKeyPair,
            joinRequest,
            JoinResponse(jsonConverter.deserialize(joinResponse.body()), publicParameters)
        )


        // Delete token if it is not anymore valid and use dummy token as new token
        // Set token using if expression
        token = if (invalidateToken) {
            cryptoDao.deleteAllTokens()
            cryptoDao.insertToken(Token(serializedToken = jsonConverter.serialize(dummyToken.representation)))
            dummyToken
        } else {
            // Deserialize current token (we use [0] bc later there will be multiple tokens)
            CryptoModelToken(
                jsonConverter.deserialize(
                    cryptoDao.getTokens()[0].serializedToken
                ), publicParameters
            )
        }

        // Setup successful, so we can set finished to true and trust this at the next application start
        cryptoDao.insertAsset(SerializedCryptoAsset(SETUP_FINISHED, TRUE))
    }

    suspend fun runCreditEarn(basketId: UUID, basketValue: Int) {
        val earnRequest = incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair)
        val earnResponse = CreditEarnApi.retrofitService.runCreditEarn(
            basketId,
            jsonConverter.serialize(earnRequest.representation)
        )

        Timber.i("Earn response $earnResponse")

        // The basket service computes the value in the backend, so no need to send it over the wire
        token = incentiveSystem.handleEarnRequestResponse(
            earnRequest,
            SPSEQSignature(
                jsonConverter.deserialize(earnResponse.body()),
                publicParameters.bg.g1,
                publicParameters.bg.g2
            ),
            BigInteger.valueOf(basketValue.toLong()),
            token,
            providerPublicKey,
            userKeyPair
        )

        // Update token in database
        // Will be nicer when handling multiple tokens
        cryptoDao.deleteAllTokens()
        cryptoDao.insertToken(Token(serializedToken = jsonConverter.serialize(token.representation)))
        Timber.i("Added new token $token to database")
    }


    companion object {

        @Volatile
        private var INSTANCE: CryptoRepository? = null

        fun getInstance(context: Context): CryptoRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CryptoRepository(context).also { INSTANCE = it }
            }
    }
}