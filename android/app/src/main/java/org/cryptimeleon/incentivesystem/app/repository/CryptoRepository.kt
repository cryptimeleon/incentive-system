package org.cryptimeleon.incentivesystem.app.repository

import android.content.Context

const val PREFERENCE_NAME = "CRYPT_PREFERENCES"
const val PUBLIC_PARAMETERS = "PUBLIC_PARAMETERS"
const val PROVIDER_PUBLIC_KEY = "PROVIDER_PUBLIC_KEY"
const val USER_PUBLIC_KEY = "USER_PUBLIC_KEY"
const val USER_PRIVATE_KEY = "USER_PRIVATE_KEY"
const val SETUP_FINISHED = "SETUP_FINISHED"
const val PROVIDER_SECRET_KEY = "PROVIDER_SECRET_KEY"
const val TOKEN = "TOKEN"


/*
 * A simple key-value store for cryptographic data that does not need to be stored in a database.
 */
class CryptoRepository(context: Context) : SharedPrefRepository(context, PREFERENCE_NAME) {

    fun setPublicParameters(publicParameters: String) {
        PUBLIC_PARAMETERS.put(publicParameters)
    }

    fun getPublicParameters(): String {
        return PUBLIC_PARAMETERS.getString()
    }

    fun setProviderPublicKey(providerPublicKey: String) {
        PROVIDER_PUBLIC_KEY.put(providerPublicKey)
    }

    fun getProviderPublicKey(): String {
        return PROVIDER_PUBLIC_KEY.getString()
    }

    fun setProviderSecretKey(providerSecretKey: String) {
        PROVIDER_SECRET_KEY.put(providerSecretKey)
    }

    fun getProviderSecretKey(): String {
        return PROVIDER_SECRET_KEY.getString()
    }

    fun setUserSecretKey(userSecretKey: String) {
        USER_PRIVATE_KEY.put(userSecretKey)
    }

    fun getUserSecretKey(): String {
        return USER_PRIVATE_KEY.getString()
    }

    fun setUserPublicKey(userPublicKey: String) {
        USER_PUBLIC_KEY.put(userPublicKey)
    }

    fun getUserPublicKey(): String {
        return USER_PUBLIC_KEY.getString()
    }

    fun setSetupFinished(finished: Boolean) {
        SETUP_FINISHED.put(finished)
    }

    fun getSetupFinished(): Boolean {
        return SETUP_FINISHED.getBoolean()
    }

    fun setToken(token: String) {
        TOKEN.put(token)
    }

    fun getToken(): String {
        return TOKEN.getString()
    }
}