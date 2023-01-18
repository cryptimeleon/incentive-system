package org.cryptimeleon.incentive.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: IPreferencesRepository,
    application: Application
) : AndroidViewModel(application) {

    fun setUserData(name: String) {
        viewModelScope.launch {
            preferencesRepository.setUserData(name)
        }
    }
}