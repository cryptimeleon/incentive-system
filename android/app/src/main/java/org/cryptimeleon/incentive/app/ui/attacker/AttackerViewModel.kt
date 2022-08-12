package org.cryptimeleon.incentive.app.ui.attacker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.cryptimeleon.incentive.app.data.network.DosApiService
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AttackerViewModel @Inject constructor(
    private val preferencesRepository: IPreferencesRepository,
    private val dosApiService: DosApiService,
    application: Application
) :
    AndroidViewModel(application) {
    val doubleSpendingPreferencesFlow: Flow<DoubleSpendingPreferences> =
        preferencesRepository.doubleSpendingPreferencesFlow

    fun setDiscardUpdatedToken(discardToken: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDiscardUpdatedToken(discardToken)
        }
    }

    fun launchShortDosAttack() {
        viewModelScope.launch {
            dosApiService.launchShortDosAttack()
            Timber.i("short dos")
        }
    }
}
