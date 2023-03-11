package org.cryptimeleon.incentive.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.model.Store

class StoreSelectionRepository(private val preferencesRepository: IPreferencesRepository) {
    val stores: List<Store>
        get() = listOf(
            Store("Default Store", "basket"),
            Store("Offline Store", "basket-two")
        )
    val defaultStore = stores[0]

    val currentStore: Flow<Store> = preferencesRepository.currentStoreName.map {
        stores.find { store -> store.name == it } ?: defaultStore
    }

    suspend fun setCurrentStore(store: Store) {
        preferencesRepository.setCurrentStoreName(store.name)
    }
}