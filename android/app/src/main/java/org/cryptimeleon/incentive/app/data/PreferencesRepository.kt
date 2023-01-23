package org.cryptimeleon.incentive.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
import java.io.IOException

class PreferencesRepository(private val dataStore: DataStore<Preferences>) :
    IPreferencesRepository {

    private object PreferencesKeys {
        val DISCARD_UPDATED_TOKEN = booleanPreferencesKey("discard_updated_token")
        val USER_DATA = stringPreferencesKey("user_data")
    }

    override val doubleSpendingPreferencesFlow: Flow<DoubleSpendingPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // Get our show completed value, defaulting to false if not set:
            val showCompleted = preferences[PreferencesKeys.DISCARD_UPDATED_TOKEN] ?: false
            DoubleSpendingPreferences(showCompleted)
        }

    override val userDataPreferencesFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_DATA] ?: "Cryptimeleon User"
        }

    override suspend fun updateDiscardUpdatedToken(discardToken: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISCARD_UPDATED_TOKEN] = discardToken
        }
    }

    override suspend fun setUserData(userData: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_DATA] = userData
        }
    }
}
