package org.cryptimeleon.incentive.app.ui.onboarding

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.BuildConfig
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: IPreferencesRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _serverUrl = MutableStateFlow(BuildConfig.SERVER_URL)
    val name = generateName()
    val serverUrl: StateFlow<String>
        get() = _serverUrl
    val serverUrlValid = serverUrl.map { Patterns.WEB_URL.matcher(it).matches() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true)

    fun setServerUrl(serverUrl: String) {
        this._serverUrl.value = serverUrl
    }

    fun storeData() {
        runBlocking {
            preferencesRepository.setUserData(name.trim())
            preferencesRepository.setServerUrl(if (serverUrl.value != "") serverUrl.value.trim() else BuildConfig.SERVER_URL)
        }
    }
}