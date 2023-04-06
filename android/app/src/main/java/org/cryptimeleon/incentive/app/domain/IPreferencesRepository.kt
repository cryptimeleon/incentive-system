package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences

/**
 * Repository for (Shared)Preferences.
 */
interface IPreferencesRepository {
    val doubleSpendingPreferencesFlow: Flow<DoubleSpendingPreferences>
    val userDataPreferencesFlow: Flow<String>
    val currentStoreName: Flow<String>
    val serverUrl: Flow<String>
    suspend fun updateDiscardUpdatedToken(discardToken: Boolean)
    suspend fun updateStopAfterPayment(requestToken: Boolean)
    suspend fun setUserData(userData: String)
    suspend fun setCurrentStoreName(name: String)
    suspend fun setServerUrl(serverUrl: String)
}
