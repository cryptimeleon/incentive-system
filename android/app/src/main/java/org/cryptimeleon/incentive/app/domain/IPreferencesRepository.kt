package org.cryptimeleon.incentive.app.domain

import kotlinx.coroutines.flow.Flow
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences

/**
 * Repository for (Shared)Preferences.
 */
interface IPreferencesRepository {
    val doubleSpendingPreferencesFlow: Flow<DoubleSpendingPreferences>
    suspend fun updateDiscardUpdatedToken(discardToken: Boolean)
    suspend fun setUserData(userData: String)
    val userDataPreferencesFlow: Flow<String>
}
