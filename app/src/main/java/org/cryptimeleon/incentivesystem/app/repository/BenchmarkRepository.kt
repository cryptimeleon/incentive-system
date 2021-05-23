package org.cryptimeleon.incentivesystem.app.repository

import android.content.Context

// Some values for the benchmark
private const val BM_PREFERENCE_NAME = "BM_CRYPTO_PREFERENCES"
private const val BM_PUBLIC_PARAMETERS = "BM_PUBLIC_PARAMETERS"
private const val BM_PROVIDER_PUBLIC_KEY = "BM_PROVIDER_PUBLIC_KEY"
private const val BM_PROVIDER_SECRET_KEY = "BM_PROVIDER_SECRET_KEY"
private const val BM_USER_PUBLIC_KEY = "BM_USER_PUBLIC_KEY"
private const val BM_USER_PRIVATE_KEY = "BM_USER_PRIVATE_KEY"
private const val BM_SETUP_FINISHED = "BM_SETUP_FINISHED"

class BenchmarkRepository(context: Context) : SharedPrefRepository(context, BM_PREFERENCE_NAME) {

    fun setPublicParameters(publicParameters: String) {
        BM_PUBLIC_PARAMETERS.put(publicParameters)
    }

    fun getPublicParameters(): String {
        return BM_PUBLIC_PARAMETERS.getString()
    }

    fun setProviderPublicKey(providerPublicKey: String) {
        BM_PROVIDER_PUBLIC_KEY.put(providerPublicKey)
    }

    fun getProviderPublicKey(): String {
        return BM_PROVIDER_PUBLIC_KEY.getString()
    }

    fun setProviderSecretKey(providerSecretKey: String) {
        BM_PROVIDER_SECRET_KEY.put(providerSecretKey)
    }

    fun getProviderSecretKey(): String {
        return BM_PROVIDER_SECRET_KEY.getString()
    }

    fun setUserSecretKey(userSecretKey: String) {
        BM_USER_PRIVATE_KEY.put(userSecretKey)
    }

    fun getUserSecretKey(): String {
        return BM_USER_PRIVATE_KEY.getString()
    }

    fun setUserPublicKey(userPublicKey: String) {
        BM_USER_PUBLIC_KEY.put(userPublicKey)
    }

    fun getUserPublicKey(): String {
        return BM_USER_PUBLIC_KEY.getString()
    }

    fun setSetupFinished(finished: Boolean) {
        BM_SETUP_FINISHED.put(finished)
    }

    fun getSetupFinished(): Boolean {
        return BM_SETUP_FINISHED.getBoolean()
    }
}