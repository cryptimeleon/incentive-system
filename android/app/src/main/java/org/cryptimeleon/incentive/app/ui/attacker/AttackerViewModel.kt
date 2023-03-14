package org.cryptimeleon.incentive.app.ui.attacker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.model.DoubleSpendingPreferences
import javax.inject.Inject

@HiltViewModel
class AttackerViewModel @Inject constructor(
    private val preferencesRepository: IPreferencesRepository,
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

    fun setStopAfterPayment(stopAfterPayment: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStopAfterPayment(stopAfterPayment)
        }
    }
}
