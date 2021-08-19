package org.cryptimeleon.incentive.app.common

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*

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