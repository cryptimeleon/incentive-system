package org.cryptimeleon.incentive.app.database.crypto

import android.content.Context
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey
import org.cryptimeleon.math.serialization.converter.JSONConverter
import timber.log.Timber

const val PP = "PUBLIC_PARAMETERS"
const val USER_PUBLIC_KEY = "USER_PUBLIC_KEY"
const val USER_SECRET_KEY = "USER_SECRET_KEY"
const val PROVIDER_PUBLIC_KEY = "PROVIDER_PUBLIC_KEY"
const val SETUP_FINISHED = "SETUP_FINISHED"
const val TRUE = "TRUE"
const val FALSE = "FALSE" // not really needed, but looks nicer

class CryptoRepository(context: Context) {
    private val jsonConverter = JSONConverter()
    private val cryptoDao = CryptoDatabase.getInstance(context).cryptoDatabaseDao()
    lateinit var publicParameters: IncentivePublicParameters
    lateinit var incentiveSystem: IncentiveSystem
    lateinit var userKeyPair: UserKeyPair
    lateinit var providerPublicKey: ProviderPublicKey

    /**
     * Initialization algorithm that takes freshly queried pp and provider public key,
     * sets up the incentive system, deletes invalid tokens/keys and stores everything to the database
     * Return true if successful
     */
    fun setup(serializedPP: String, serializedProviderPublicKey: String) {
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

        // Delete token if it is not anymore valid
        if (invalidateToken) {
            cryptoDao.deleteAllTokens()
        }

        // Setup successful, so we can set finished to true and trust this at the next application start
        cryptoDao.insertAsset(SerializedCryptoAsset(SETUP_FINISHED, TRUE))
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
