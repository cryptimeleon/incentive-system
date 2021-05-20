package org.cryptimeleon.incentivesystem.app.crypto

import android.content.Context
import android.content.SharedPreferences
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey

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
class CryptoRepository(context: Context) {
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

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


    // Some functions that extend string to make the api a little nices
    // See: https://arkapp.medium.com/how-to-use-shared-preferences-the-easy-and-fastest-way-98ce2013bf51

    private fun String.put(string: String) {
        editor.putString(this, string)
        editor.commit()
    }

    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private fun String.getString() = pref.getString(this, "")!!

    private fun String.getBoolean() = pref.getBoolean(this, false)
}