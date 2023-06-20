package org.cryptimeleon.incentive.app.ui.onboarding

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    val serverUrl: StateFlow<String>
        get() = _serverUrl
    val serverUrlValid = serverUrl.map { Patterns.WEB_URL.matcher(it).matches() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true)
    private val _name = MutableStateFlow("John Doe")
    val name: StateFlow<String>
        get() = _name

    fun setUserData(name: String) {
        this._name.value = name
    }

    fun setServerUrl(serverUrl: String) {
        this._serverUrl.value = serverUrl
    }

    fun storeData() {
        runBlocking {
            preferencesRepository.setUserData(name.value.trim())
            preferencesRepository.setServerUrl(if (serverUrl.value != "") serverUrl.value.trim() else BuildConfig.SERVER_URL)
        }
    }
}