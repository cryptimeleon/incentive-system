package org.cryptimeleon.incentive.app.common

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun DefaultTopAppBar(onOpenSettings: () -> Unit = {}, onOpenBenchmark: () -> Unit = {}) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Cryptimeleon Rewards") },

        actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.MoreVert, "Menu Icon")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(onClick = onOpenSettings) {
                    Text("Settings")
                }
                DropdownMenuItem(onClick = onOpenBenchmark) {
                    Text("Benchmark")
                }
            }
        }
    )
}
