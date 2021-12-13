package org.cryptimeleon.incentive.app.ui.common

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun DefaultTopAppBar(
    onOpenSettings: () -> Unit = {},
    onOpenBenchmark: () -> Unit = {},
    title: @Composable () -> Unit = { Text("Cryptimeleon Rewards") },
    navigationIcon: @Composable (() -> Unit)? = null,
    menuEnabled: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = title,
        contentPadding = rememberInsetsPaddingValues(
            LocalWindowInsets.current.statusBars,
            applyBottom = false,
        ),
        navigationIcon = navigationIcon,
        actions = {
            if (menuEnabled) {
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
        }
    )
}
