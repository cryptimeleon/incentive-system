package org.cryptimeleon.incentive.app.ui.storeselection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.cryptimeleon.incentive.app.domain.model.Store

@Composable
fun StoreSelectionSheet(onDismissRequest: () -> Unit) {
    val storeSelectionViewModel = hiltViewModel<SelectStoreViewModel>()
    val currentStore by storeSelectionViewModel.currentStore.collectAsState()
    val stores = storeSelectionViewModel.stores

    StoreSelectionSheet(onDismissRequest, stores, currentStore, storeSelectionViewModel::setStore)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StoreSelectionSheet(
    onDismissRequest: () -> Unit,
    stores: List<Store>,
    currentStore: Store,
    onStoreSelected: (Store) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Choose your store", style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.selectableGroup()) {
                for (store in stores) {
                    Surface(
                        onClick = {
                            onStoreSelected(store)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = currentStore == store,
                                onClick = {}
                            )
                            Text(store.name)
                        }
                    }
                }
            }
        }
    }
}
