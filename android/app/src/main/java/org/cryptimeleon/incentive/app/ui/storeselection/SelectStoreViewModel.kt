package org.cryptimeleon.incentive.app.ui.storeselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cryptimeleon.incentive.app.data.StoreSelectionRepository
import org.cryptimeleon.incentive.app.domain.model.Store
import javax.inject.Inject

@HiltViewModel
class SelectStoreViewModel @Inject constructor(
    private val storeSelectionRepository: StoreSelectionRepository,
    application: Application
) :
    AndroidViewModel(application) {
    val stores: List<Store> = storeSelectionRepository.stores
    val currentStore: StateFlow<Store> = storeSelectionRepository.currentStore.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        storeSelectionRepository.defaultStore
    )

    fun setStore(store: Store) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                storeSelectionRepository.setCurrentStore(store)
            }
        }
    }
}