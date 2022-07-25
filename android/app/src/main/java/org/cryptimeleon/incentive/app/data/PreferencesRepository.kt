package org.cryptimeleon.incentive.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
import java.io.IOException

private const val DOUBLE_SPENDING_PREFERENCES_NAME = "double_spending_preferences"

class PreferencesRepository(private val dataStore: DataStore<Preferences>) :
    IPreferencesRepository {

    private object PreferencesKeys {
        val DISCARD_UPDATED_TOKEN = booleanPreferencesKey("discard_updated_token")
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

    override suspend fun updateDiscardUpdatedToken(discardToken: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISCARD_UPDATED_TOKEN] = discardToken
        }
    }
}
