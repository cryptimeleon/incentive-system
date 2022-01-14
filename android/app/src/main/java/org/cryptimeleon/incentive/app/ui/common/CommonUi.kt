package org.cryptimeleon.incentive.app.ui.common

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    Icon(Icons.Default.Settings, "Menu Icon")
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
